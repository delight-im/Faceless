<?php

require_once(__DIR__.'/config.php');
require_once(__DIR__.'/public_html/classes/Database.php');

// SERVER AND PAGE CONFIGURATION BEGIN
header('Content-type: application/json; charset=utf-8');
header('X-Bug-Bounty: https://www.delight.im/security/faceless');
if (CONFIG_API_DEBUG) {
    error_reporting(E_ALL);
    ini_set('display_errors', 'stdout');
}
header('Vary: Accept-Encoding');
ob_start('ob_gzhandler') or ob_start();
// SERVER AND PAGE CONFIGURATION END

// HOST-SPECIFIC FUNCTIONS (HEROKU) BEGIN
function isHTTPS() {
    return isset($_SERVER['HTTP_X_FORWARDED_PROTO']) && $_SERVER['HTTP_X_FORWARDED_PROTO'] === 'https';
}
// HOST-SPECIFIC FUNCTIONS (HEROKU) END

function respond($data) {
    // if the response is an array
    if (is_array($data)) {
        // add a random padding (nonce)
        $data['_rand'] = getRandomPadding();
    }
    // return as JSON
    echo json_encode($data);
    // and exit
    exit;
}
function getRequestIdentifier($requestData) {
    $restPath = explode('?', $_SERVER['REQUEST_URI'], 2);
    $restPath = $restPath[0];
    $timestamp = isset($_SERVER[CONFIG_HEADER_TIMESTAMP]) ? $_SERVER[CONFIG_HEADER_TIMESTAMP] : '';
    $username = isset($_SERVER['PHP_AUTH_USER']) ? $_SERVER['PHP_AUTH_USER'] : '';

    return $restPath.'#'.http_build_query($requestData, '', '&').'#'.$timestamp.'#'.$username;
}
function init($requestData) {
    if (CONFIG_API_LIVE) {
        if (!CONFIG_ENFORCE_SSL || isHTTPS()) {
            // if the client uses HTTPS (or it's not enforced)
            if (isset($_SERVER['HTTP_USER_AGENT'])) {
                // if User-Agent header is set
                if (preg_match('/Faceless-([a-z_-]+)-([0-9]+)/i', $_SERVER['HTTP_USER_AGENT'], $userAgent)) {
                    // if User-Agent header has valid format
                    $userAgentList = unserialize(CONFIG_API_CLIENTS);
                    // if user agent uses valid platform and no outdated client software
                    if (isset($userAgentList[$userAgent[1]]) && intval($userAgent[2]) >= $userAgentList[$userAgent[1]]) {
                        // get the custom request timestamp from the HTTP headers
                        $requestTimestamp = isset($_SERVER[CONFIG_HEADER_TIMESTAMP]) ? $_SERVER[CONFIG_HEADER_TIMESTAMP] : '';
                        // if the timestamp (verified by signature) is within the valid interval
                        if (abs($requestTimestamp-time()) < 86400) {
                            // get the signature provided by the client
                            $signatureClient = isset($_SERVER[CONFIG_HEADER_SIGNATURE]) ? $_SERVER[CONFIG_HEADER_SIGNATURE] : '';
                            // generate the server signature for this request
                            $signatureServer = base64_encode(hash_hmac(CONFIG_HMAC_ALGORITHM, getRequestIdentifier($requestData), CONFIG_API_SECRET, true));
                            // compare the server signature to the client signature for request verification
                            if (hash_equals($signatureClient, $signatureServer)) {
                                // integrity and authenticity of the request have been verified

                                // initialize the database connection
                                try {
                                    Database::init(CONFIG_DB_CONNECT_STRING, CONFIG_DB_USERNAME, CONFIG_DB_PASSWORD);
                                }
                                catch (Exception $e) {
                                    respond(array('status' => 'maintenance'));
                                }

                                // return with supplied user credentials
                                return array(
                                    'username' => isset($_SERVER['PHP_AUTH_USER']) ? $_SERVER['PHP_AUTH_USER'] : '',
                                    'password' => isset($_SERVER['PHP_AUTH_PW']) ? $_SERVER['PHP_AUTH_PW'] : ''
                                );
                            }
                            else {
                                respond(array('status' => 'bad_request'));
                            }
                        }
                        else {
                            respond(array('status' => 'bad_request'));
                        }
                    }
                    else {
                        respond(array('status' => 'outdated_client'));
                    }
                }
                else {
                    respond(array('status' => 'bad_request'));
                }
            }
            else {
                respond(array('status' => 'bad_request'));
            }
        }
        else {
            respond(array('status' => 'bad_request'));
        }
    }
    else {
        respond(array('status' => 'maintenance'));
    }
    return NULL; // suppress IDE warnings
}
function createKey($messageSecret) {
    return hash_hmac('sha512', $messageSecret, CONFIG_SERVER_SECRET);
}
function makeHash($input) {
    return base64_encode(hash('sha256', $input.CONFIG_SERVER_SECRET, true));
}
function auth($username, $password, $requiresWrite) {
    $u = Database::escape(makeHash($username));
    $pUnescaped = makeHash($password);

    Database::insert("INSERT INTO users (username, password, time_last_active, time_registered) VALUES (".$u.", ".Database::escape($pUnescaped).", ".time().", ".time().") ON DUPLICATE KEY UPDATE time_registered = IF(time_registered IS NULL, VALUES(time_registered), time_registered), password = IF(password IS NULL, VALUES(password), password), time_last_active = VALUES(time_last_active)");
    $res = Database::selectFirst("SELECT id, password, write_lock_until, login_throttled_until FROM users WHERE username = ".$u);
    // if a user with the given username exists
    if (isset($res['id'])) {
        // if the login has been throttled for this user
        if ($res['login_throttled_until'] > time()) {
            respond(array('status' => 'login_throttled'));
        }
        // if the user's login is not throttled
        else {
            // if the user's password has been correct
            if ($res['password'] === $pUnescaped) {
                // if there is a write log for this user because they have been flagged by other users
                if ($requiresWrite && $res['write_lock_until'] > time()) {
                    respond(array('status' => 'temporarily_banned'));
                }
                // if everything was okay
                else {
                    return $res['id'];
                }
            }
            // if the user provided a wrong password
            else {
                // see how many failed attempts there have been for this user already
                $date_str = Database::escape(date('Ymd'));
                $throttling = Database::selectFirst("SELECT action_count FROM throttling WHERE username = ".$u." AND date_str = ".$date_str." AND action_type = 'failed_login'");
                // if the number of failed attempts has reached the critical threshold
                if (isset($throttling['action_count']) && $throttling['action_count'] >= CONFIG_THROTTLING_LOGIN_ATTEMPTS) {
                    // throttle the user's login for some time
                    $throttlingTimeout = intval(time() + (3600 * CONFIG_THROTTLING_LOGIN_HOURS));
                    Database::update("UPDATE users SET login_throttled_until = ".$throttlingTimeout." WHERE username = ".$u);
                }
                // if the number of failed attempts is not critical yet
                else {
                    // just remember this failed attempt and add it to the counter for this user
                    Database::insert("INSERT INTO throttling (username, date_str, action_type) VALUES (".$u.", ".$date_str.", 'failed_login') ON DUPLICATE KEY UPDATE action_count = action_count+1");
                }
                respond(array('status' => 'not_authorized'));
            }
        }
    }
    // if a user with that username does not exist
    else {
        respond(array('status' => 'not_authorized'));
    }
    return NULL; // suppress IDE warnings
}
function getScoreUpdateSQL() {
    $formula = "POW(1+favorites_count+comments_count/4, 0.85)/POW((".time()."-time_published)/86400, 1.25)";
	return "IF(time_published < ".time().", ".$formula.", 0)";
}
function getDegree($userID, $messageID) {
    $degree = Database::selectFirst("SELECT degree FROM feeds WHERE user_id = ".intval($userID)." AND message_id = ".intval($messageID));
    // if there is already an existing connection (degree available)
    if (isset($degree['degree'])) {
        // return this degree
        return $degree['degree'];
    }
    else {
        // otherwise default to degree 3 (no direct connection anymore but worldwide)
        return 3;
    }
}

if (!function_exists('hash_equals')) {

    /**
     * Use HMAC with a nonce to compare two strings in a manner that is resistant to timing attacks
     *
     * Shim for older PHP versions providing support for the PHP >= 5.6.0 built-in function
     *
     * @param $a string first hash
     * @param $b string second hash
     * @return boolean true if the strings are the same, false otherwise
     */
    function hash_equals($a, $b) {
        $nonce = mcrypt_create_iv(32, MCRYPT_DEV_URANDOM);
        return hash_hmac('sha256', $a, $nonce, true) === hash_hmac('sha256', $b, $nonce, true);
    }

}

/**
 * Returns a random string (between 1 and 32 chars) to pad responses with as a protection against BREACH
 *
 * @author Anthony Ferrara
 */
function getRandomPadding() {
    $randomData = mcrypt_create_iv(25, MCRYPT_DEV_URANDOM);
    return substr(base64_encode($randomData), 0, ord($randomData[24]) % 32);
}

?>