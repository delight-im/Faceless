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
require_once(__DIR__.'/../base_crypto.php');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // initialization
    $user = init($_POST);
    // force authentication
    $userID = auth($user['username'], $user['password'], true);
    // check if required parameters are set
    if (isset($_POST['colorHex']) && isset($_POST['patternID']) && isset($_POST['text']) && isset($_POST['topic']) && isset($_POST['random'])) {
        // check color string (hex) for validity
        if (preg_match("/^#[abcdef0-9]{6}$/i", $_POST['colorHex'])) {
            // require at least 32 characters for the random string
            if (strlen($_POST['random']) >= 32) {
                // require the pattern ID to be positive
                if ($_POST['patternID'] >= 0) {
                    $messageSecret = makeHash($_POST['random']);
                    $textEncrypted = encrypt(trim($_POST['text']), $messageSecret);

                    // save the message
                    $timePublished = time();
                    $messageFields = "user_id, color_hex, pattern_id, text_encrypted, message_secret, time_published, time_active, topic";
                    $messageValues = intval($userID).", ".Database::escape($_POST['colorHex']).", ".intval($_POST['patternID']).", ".Database::escape($textEncrypted).", ".Database::escape($messageSecret).", ".$timePublished.", ".$timePublished.", ".Database::escape($_POST['topic']);
                    if (isset($_POST['languageISO3'])) {
                        $messageFields .= ", language_iso3";
                        $messageValues .= ", ".Database::escape($_POST['languageISO3']);
                    }
                    if (isset($_POST['countryISO3'])) {
                        $messageFields .= ", country_iso3";
                        $messageValues .= ", ".Database::escape($_POST['countryISO3']);
                    }
                    Database::insert("INSERT INTO messages (".$messageFields.") VALUES (".$messageValues.")");
                    $messageID = Database::getLastInsertID();

                    // add the message to the author's feed
                    Database::insert("INSERT INTO feeds (user_id, message_id, degree) VALUES (".intval($userID).", ".intval($messageID).", 0)");

                    // subscribe to the comments thread
                    Database::insert("INSERT IGNORE INTO subscriptions (message_id, user_id, degree) VALUES (".intval($messageID).", ".intval($userID).", 0)");

                    respond(array(
                        'status' => 'ok',
                        'messageID' => base64_encode($messageID),
                        'messageTime' => $timePublished
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
}
else {
	respond(array('status' => 'bad_request'));
}

?>