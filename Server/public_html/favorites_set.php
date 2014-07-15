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
    $user = init($_POST);
    // force authentication
    $userID = auth($user['username'], $user['password'], true);
    // check if required parameters are set
    if (isset($_POST['messageID']) && isset($_POST['favorited'])) {
        $messageID = intval(base64_decode(trim($_POST['messageID'])));
        $favorited = $_POST['favorited'] == 1;

        if ($favorited) {
            // get the existing degree (if any) or 3 (default)
            $degree = getDegree($userID, $messageID);

            // try to add the message as a personal favorite
            $success = Database::insert("INSERT INTO favorites (user_id, message_id, degree, time_added) VALUES (".intval($userID).", ".intval($messageID).", ".intval($degree).", ".time().")");
            // if the action succeeded (the favorite is new)
            if ($success) {
                // update the favorites count of the message
                Database::update("UPDATE messages SET favorites_count = favorites_count+1, score = ".getScoreUpdateSQL()." WHERE id = ".intval($messageID));

                // if the message is a (friend of a) friend's message
                if ($degree == 1 || $degree == 2) {
                    // forward the message to all users who have the authenticating user as their friend
                    // increase the degree by one and replace existing entries if the degree is now lower (shorter connection between author and receiver of message)
                    Database::insert("INSERT INTO feeds (user_id, message_id, degree) SELECT from_user AS user_id, ".intval($messageID)." AS message_id, ".intval($degree+1)." AS degree FROM connections WHERE to_user = ".intval($userID)." AND type = 'friend' ON DUPLICATE KEY UPDATE degree = IF(VALUES(degree) < degree, VALUES(degree), degree)");
                }
            }
            // if the action failed (already in favorites)
            else {
                // just update the time to put the message to the top of the favorites list again
                Database::update("UPDATE favorites SET time_added = ".time()." WHERE user_id = ".intval($userID)." AND message_id = ".intval($messageID));
            }
        }
        else {
            Database::delete("DELETE FROM favorites WHERE user_id = ".intval($userID)." AND message_id = ".intval($messageID));
        }
        respond(array('status' => 'ok'));
    }
    else {
        respond(array('status' => 'bad_request'));
    }
}
else {
	respond(array('status' => 'bad_request'));
}

?>