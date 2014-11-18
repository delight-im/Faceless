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

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // initialization
    $user = init($_GET);
    // force authentication
    $userID = auth($user['username'], $user['password'], false);
    // check if required parameters are set
    if (isset($_GET['messageID'])) {
        $messageID = intval(base64_decode(trim($_GET['messageID'])));

        // prepare temporary array for comments
        $comments = array();

        // get the parent message's data
        $parentMessageData = Database::selectFirst("SELECT user_id FROM messages WHERE id = ".intval($messageID));
        if (empty($parentMessageData)) {
            $parentMessageData = array('user_id' => NULL);
        }

        // get the public IDs for all users in this comments thread
        $publicUserIDs = UserIDsInThread::get($messageID);

        // mark this comments thread as read
        Database::update("UPDATE subscriptions SET counter = 0 WHERE message_id = ".intval($messageID)." AND user_id = ".intval($userID));

        // check if the authenticating user is an admin user
        $isAdmin = in_array($userID, unserialize(CONFIG_ADMIN_USER_IDS));

        // get the comments for the given message
        $commentsQuery = "SELECT id, user_id, text_encrypted, comment_secret, private_to_user, time_inserted FROM comments WHERE message_id = ".intval($messageID);
        // the content must either not have been deleted (flagged through reports) or the authenticating user must be the author of the content themself
        $commentsQuery .= " AND (deleted = 0 OR user_id = ".intval($userID).")";
        // unless the authenticating user has administrator privileges and those permissions allow the inspection of private conversations
        if (!$isAdmin || !CONFIG_ADMINS_READ_PRIVATE) {
            // the content must either be public or the authenticating user must be the designated sender/recipient from the private conversation
            $commentsQuery .= " AND (private_to_user IS NULL OR private_to_user = ".intval($userID)." OR user_id = ".intval($userID).")";
        }
        // the items are sorted by freshness and the total number is limited as set in the configuration
        $commentsQuery .= " ORDER BY time_inserted DESC LIMIT 0, ".CONFIG_COMMENTS_PER_PAGE;
        // run the query and get the results
        $items = Database::select($commentsQuery);

        // return the comments
        foreach ($items as $item) {
            // try to decrypt the content
            $textDecrypted = decrypt($item['text_encrypted'], $item['comment_secret']);

            // if the content has just been successfully decrypted
            if ($textDecrypted !== false) {
                $comments[] = array(
                    'id' => base64_encode($item['id']),
                    'text' => $textDecrypted,
                    'privateRecipientInThread' => isset($publicUserIDs[$item['private_to_user']]) ? $publicUserIDs[$item['private_to_user']] : NULL,
                    'isOwner' => ($item['user_id'] == $parentMessageData['user_id']),
                    'isSelf' => ($item['user_id'] == $userID),
                    'ownerInThread' => isset($publicUserIDs[$item['user_id']]) ? $publicUserIDs[$item['user_id']] : NULL,
                    'time' => $item['time_inserted']
                );
            }
        }

        respond(array(
            'status' => 'ok',
            'comments' => $comments
        ));
    }
    else {
        respond(array('status' => 'bad_request'));
    }
}
else {
	respond(array('status' => 'bad_request'));
}

?>