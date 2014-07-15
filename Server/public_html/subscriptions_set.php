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
    if (isset($_POST['messageID']) && isset($_POST['subscribed'])) {
        $messageID = intval(base64_decode(trim($_POST['messageID'])));
        $subscribed = $_POST['subscribed'] == 1;

        if ($subscribed) {
            // get the existing degree (if any) or 3 (default)
            $degree = getDegree($userID, $messageID);

            Database::insert("INSERT IGNORE INTO subscriptions (message_id, user_id, degree) VALUES (".intval($messageID).", ".intval($userID).", ".intval($degree).")");
        }
        else {
            Database::delete("DELETE FROM subscriptions WHERE message_id = ".intval($messageID)." AND user_id = ".intval($userID));
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