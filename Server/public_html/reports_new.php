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

function getReportPower($recentReports) {
    if (isset($recentReports)) {
        switch ($recentReports) {
            case 0: return 90;
            case 1: return 86;
            case 2: return 78;
            case 3: return 66;
            case 4: return 50;
            case 5: return 30;
            case 6: return 6;
            default: return 4;
        }
    }
    else {
        return 4;
    }
}

function setUserReported($contentOriginTable, $contentID, $isAdmin) {
    // get the ID of the user who was reported
    $reportedUserID = Database::selectFirst("SELECT user_id FROM ".$contentOriginTable." WHERE id = ".intval($contentID));
    $reportedUserID = $reportedUserID['user_id'];

    // get the ID of the message thread that the user was reported in
    if ($contentOriginTable == 'messages') {
        $messageID = $contentID;
    }
    elseif ($contentOriginTable == 'comments') {
        $messageID = Database::selectFirst("SELECT message_id FROM comments WHERE id = ".$contentID);
        $messageID = $messageID['message_id'];
    }
    else {
        // we can't handle this request
        respond(array('status' => 'bad_request'));
        // prevent IDE warnings
        exit;
    }

    // mark the user as reported and possibly ban them temporarily
    $possibleWriteLockEnd = time()+3600*24*5;
    $timesReported = $isAdmin ? 2 : 1;
    Database::update("UPDATE users SET reported_count = reported_count+".$timesReported.", write_lock_until = IF(reported_count >= 3, ".intval($possibleWriteLockEnd).", write_lock_until), reported_count = IF(reported_count >= 3, 1, reported_count) WHERE id = ".$reportedUserID);

    // send a notice to the violating user
    Database::insert("INSERT INTO subscriptions (message_id, user_id, degree, reasonForBan, counter) VALUES (".intval($messageID).", ".$reportedUserID.", 3, 1, 1) ON DUPLICATE KEY UPDATE reasonForBan = 1, counter = 1");
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // initialization
    $user = init($_POST);
    // force authentication
    $userID = auth($user['username'], $user['password'], false);
    // check if required parameters are set
    if (isset($_POST['contentType']) && isset($_POST['contentID']) && isset($_POST['reason'])) {
        $contentID = intval(base64_decode(trim($_POST['contentID'])));
        $reason = intval(trim($_POST['reason']));
        $isAdmin = in_array($userID, unserialize(CONFIG_ADMIN_USER_IDS));
        // determine which table to update
        $contentOriginTable = $_POST['contentType'] == 'message' ? 'messages' : 'comments';

        if ($reason >= 0 && $reason <= 7) {
            if ($_POST['contentType'] == 'message' || $_POST['contentType'] == 'comment') {
                // if the authenticating user is an administrator
                if ($isAdmin) {
                    // use a fixed reporting power that allows the user to delete the post immediately
                    $reportPower = 100;
                }
                // if the authenticating user is a normal user without any special privileges
                else {
                    // get the number of reports the user has sent recently
                    $timeout = time()-3600*24*5;
                    $reportsCount = Database::selectFirst("SELECT COUNT(*) FROM reports WHERE user_id = ".intval($userID)." AND time_reported > ".intval($timeout));

                    // calculate the user's reporting power
                    if (isset($reportsCount['COUNT(*)'])) {
                        $reportPower = getReportPower($reportsCount['COUNT(*)']);
                    }
                    else {
                        respond(array('status' => 'bad_request'));
                    }
                }

                // only for reported messages
                if ($_POST['contentType'] == 'message') {
                    // immediately delete the message from the reporting user's personal feed
                    Database::delete("DELETE FROM feeds WHERE user_id = ".intval($userID)." AND message_id = ".intval($contentID));
                }

                // check whether the threshold to delete the content has been reached
                $doDeleteContent = false;
                if ($isAdmin) {
                    // increase the report counter and give the author a temporary write lock if necessary
                    setUserReported($contentOriginTable, $contentID, true);
                    // do not file a real report but just delete the content (moderation)
                    $doDeleteContent = true;
                }
                else {
                    // add the report to the list of reports
                    if (Database::insert("INSERT INTO reports (user_id, content_type, content_id, reason, weight, time_reported) VALUES (".intval($userID).", ".Database::escape($_POST['contentType']).", ".intval($contentID).", ".intval($reason).", ".intval($reportPower).", ".time().")")) {
                        // check if the threshold of aggregated reporting powers has been reached
                        $reportsWeight = Database::selectFirst("SELECT SUM(weight) FROM reports WHERE content_type = ".Database::escape($_POST['contentType'])." AND content_id = ".intval($contentID));
                        // if enough users have reported the object
                        if (isset($reportsWeight['SUM(weight)']) && $reportsWeight['SUM(weight)'] >= 100) {
                            // increase the report counter and give the author a temporary write lock if necessary
                            setUserReported($contentOriginTable, $contentID, false);
                            // delete the content as it has been flagged by users
                            $doDeleteContent = true;
                        }
                    }
                }

                // if the content has been found to be eligible for deletion
                if ($doDeleteContent) {
                    // mark the piece of content as deleted but don't really delete it so that we can still show it to its author
                    Database::delete("UPDATE ".$contentOriginTable." SET deleted = 1 WHERE id = ".intval($contentID));
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
}
else {
    respond(array('status' => 'bad_request'));
}

?>