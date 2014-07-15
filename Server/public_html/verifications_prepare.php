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

require_once(__DIR__.'/../base.php');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // initialization
    $user = init($_GET);

    // prepare username and password for internal usage with the database
    $usernameEscaped = Database::escape(makeHash($user['username']));
    $passwordEscaped = Database::escape(makeHash($user['password']));

    // get the user whose phone number we want to prepare for verification
    $verifyUser = Database::selectFirst("SELECT id FROM users WHERE username = ".$usernameEscaped." AND password IS NOT NULL AND password != ".$passwordEscaped);

    // if an existing user with the given username could be found (whose password is set but not the given one)
    if (isset($verifyUser['id'])) {
        // search for other verification requests which may still be open for this user
        $openRequests = Database::selectFirst("SELECT COUNT(*) FROM verifications WHERE user_id = ".intval($verifyUser['id'])." AND time_until > ".time());

        // if the user has fewer than 50 open verification requests (we allow some for failed attempts)
        if (isset($openRequests['COUNT(*)']) && $openRequests['COUNT(*)'] < 50) {
            $verificationCode = md5(openssl_random_pseudo_bytes(128));
            $validUntilTime = time()+3600*12;

            $success = Database::insert("INSERT INTO verifications (user_id, new_password, verification_code, time_created, time_until) VALUES (".intval($verifyUser['id']).", ".$passwordEscaped.", ".Database::escape($verificationCode).", ".time().", ".$validUntilTime.")");

            if ($success) {
                respond(array(
                    'status' => 'ok',
                    'apiPhoneNumber' => CONFIG_API_PHONE_NUMBER,
                    'verificationCode' => $verificationCode,
                    'validUntil' => $validUntilTime
                ));
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
    respond(array('status' => 'bad_request'));
}

?>