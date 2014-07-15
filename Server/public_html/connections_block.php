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
	$userID = auth($user['username'], $user['password'], false);
    // check if required parameters are set
    if (isset($_POST['contentType']) && isset($_POST['contentID'])) {
        $contentID = intval(base64_decode(trim($_POST['contentID'])));

        if ($_POST['contentType'] == 'message') {
            $authorID = Database::selectFirst("SELECT user_id FROM messages WHERE id = ".intval($contentID));
            if (isset($authorID['user_id']) && $authorID['user_id'] != $userID) {
                Database::insert("INSERT INTO connections (from_user, type, to_user, time_inserted) VALUES (".intval($userID).", 'block', ".intval($authorID['user_id']).", ".time().") ON DUPLICATE KEY UPDATE type = VALUES(type)");
            }
            respond(array('status' => 'ok'));
        }
        elseif ($_POST['contentType'] == 'comment') {
            $authorID = Database::selectFirst("SELECT user_id FROM comments WHERE id = ".intval($contentID));
            if (isset($authorID['user_id']) && $authorID['user_id'] != $userID) {
                Database::insert("INSERT INTO connections (from_user, type, to_user, time_inserted) VALUES (".intval($userID).", 'block', ".intval($authorID['user_id']).", ".time().") ON DUPLICATE KEY UPDATE type = VALUES(type)");
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
}
else {
	respond(array('status' => 'bad_request'));
}

?>