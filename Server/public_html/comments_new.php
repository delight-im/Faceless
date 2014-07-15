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
require_once(__DIR__.'/classes/UserIDsInThread.php');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // initialization
    $user = init($_POST);
    // force authentication
    $userID = auth($user['username'], $user['password'], true);
    // check if required parameters are set
    if (isset($_POST['messageID']) && isset($_POST['text']) && isset($_POST['random'])) {
        // require at least 32 characters for the random string
        if (strlen($_POST['random']) >= 32) {
            $messageID = intval(base64_decode(trim($_POST['messageID'])));
            $commentSecret = makeHash($_POST['random']);
            $textEncrypted = encrypt(trim($_POST['text']), $commentSecret);
            $text = trim($_POST['text']);

            // check if this is a private reply and, if so, get the necessary data
            $privateToUser = NULL;
            $privateRecipientInThread = NULL;
            if (isset($_POST['privateReplyToComment'])) {
                $commenterData = UserIDsInThread::getByComment($messageID, intval(base64_decode(trim($_POST['privateReplyToComment']))));

                if (isset($commenterData['user_id'])) {
                    $privateToUser = $commenterData['user_id'];
                }
                else {
                    respond(array('status' => 'bad_request'));
                }

                if (isset($commenterData['public_id'])) {
                    $privateRecipientInThread = $commenterData['public_id'];
                }
            }

            // save the comment
            $commentTime = time();
            $commentFields = "message_id, user_id, text_encrypted, comment_secret, time_inserted";
            $commentValues = intval($messageID).", ".intval($userID).", ".Database::escape($textEncrypted).", ".Database::escape($commentSecret).", ".$commentTime;
            if (isset($privateToUser)) {
                $commentFields .= ", private_to_user";
                $commentValues .= ", ".intval($privateToUser);
            }
            Database::insert("INSERT INTO comments (".$commentFields.") VALUES (".$commentValues.")");
            $commentID = Database::getLastInsertID();

            // for private comments
            if (isset($privateToUser)) {
                // update the date of the latest activity
                Database::update("UPDATE messages SET time_active = ".time()." WHERE id = ".intval($messageID));
            }
            // for public comments
            else {
                // increase the comments count by one, update the score and update the date of the latest activity
                Database::update("UPDATE messages SET comments_count = comments_count+1, score = ".getScoreUpdateSQL().", time_active = ".time()." WHERE id = ".intval($messageID));
            }

            // get the existing degree (if any) or 3 (default)
            $degree = getDegree($userID, $messageID);

            // subscribe to the comments thread (if not done already)
            Database::insert("INSERT IGNORE INTO subscriptions (message_id, user_id, degree) VALUES (".intval($messageID).", ".intval($userID).", ".intval($degree).")");

            // if this is a private reply
            if (isset($privateToUser)) {
                // notify the recipient of the private reply that there is a new comment
                Database::update("UPDATE subscriptions SET counter = counter+1 WHERE message_id = ".intval($messageID)." AND user_id = ".intval($privateToUser));
            }
            // if this is a public comment
            else {
                // notify all other subscribers that there is a new comment
                Database::update("UPDATE subscriptions SET counter = counter+1 WHERE message_id = ".intval($messageID)." AND user_id != ".intval($userID));
            }

            // create a public ID for this user that is unique within this thread (only)
            UserIDsInThread::create($messageID, $userID);

            respond(array(
                'status' => 'ok',
                'commentID' => base64_encode($commentID),
                'commentTime' => $commentTime,
                'ownerInThread' => UserIDsInThread::getByUser($messageID, $userID),
                'privateRecipientInThread' => $privateRecipientInThread
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

?>