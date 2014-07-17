<?php

/**
 * Copyright (C) 2014 www.delight.im <info@delight.im>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */

require_once(__DIR__ . '/../../base.php');

// initialize the database connection
try {
    Database::init(CONFIG_DB_CONNECT_STRING, CONFIG_DB_USERNAME, CONFIG_DB_PASSWORD);
}
catch (Exception $e) {
    throw new Exception('Could not connect to database');
}

function getTwilioEndpoint() {
    return (isHTTPS() ? 'https://' : 'http://').$_SERVER['SERVER_NAME'].$_SERVER['PHP_SELF'];
}

function getTwilioSignature($url, $authToken, $data = array()) {
    // sort the array by keys
    ksort($data);

    // append the data array to the URL without any delimiters
    foreach ($data as $key => $value) {
        $url = $url.$key.$value;
    }

    // calculate HMAC SHA-1
    $hmac = hash_hmac('sha1', $url, $authToken, true);

    // return hash as Base64
    return base64_encode($hmac);
}

function clientHash($input) {
    $output = '';
    for ($i = 0; $i < CONFIG_CLIENT_HASH_ITERATIONS; $i++) {
        $output = clientHashInternal($input.$output.CONFIG_CLIENT_HASH_SEED);
    }
    return $output;
}

function clientHashInternal($input) {
    return base64_encode(hash(CONFIG_CLIENT_HASH_ALGORITHM, $input, true));
}

function extractHexHash($text) {
    // if a hexadecimal hash (32+ chars) is found
    if (preg_match('/[abcdef0-9]{32,}/is', $text, $subpattern)) {
        // return the extracted hash
        return $subpattern[0];
    }
    else {
        // return an empty string because we didn't find the hash
        return '';
    }
}

$incomingSignature = isset($_SERVER['HTTP_X_TWILIO_SIGNATURE']) ? $_SERVER['HTTP_X_TWILIO_SIGNATURE'] : '';
$requiredSignature = getTwilioSignature(getTwilioEndpoint(), CONFIG_TWILIO_AUTH_CODE, $_POST);
if (hash_equals($incomingSignature, $requiredSignature)) {
    if (isset($_POST['From']) && isset($_POST['Body'])) {
        $incomingCode = extractHexHash($_POST['Body']);
        // try to find an open request with the given verification code
        $openRequest = Database::selectFirst("SELECT user_id, new_password FROM verifications WHERE verification_code = ".Database::escape($incomingCode)." AND time_until > ".time());

        // if an open request with the given code has been found
        if (isset($openRequest['user_id']) && isset($openRequest['new_password'])) {
            $usernameByPhoneNumber = makeHash(clientHash(trim($_POST['From'])));
            // set the new password for the user if the actual phone number matches the pretended phone number (contained in the username)
            Database::update("UPDATE users SET password = ".Database::escape($openRequest['new_password'])." WHERE id = ".intval($openRequest['user_id'])." AND username = ".Database::escape($usernameByPhoneNumber));
            // invalidate all open verification requests
            Database::update("UPDATE verifications SET time_until = 0 WHERE user_id = ".intval($openRequest['user_id']));
        }
    }
}

// overwrite the response type header
header('Content-type: application/xml; charset=utf-8');

// send an empty response for the Twilio API (do nothing) and exit
echo '<?xml version="1.0" encoding="utf-8"?>';
echo '<Response></Response>';
exit;

?>