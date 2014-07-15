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

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // initialization
    $user = init($_GET);
    // force authentication
    $userID = auth($user['username'], $user['password'], false);
    // check if required parameters are set
    if (isset($_GET['messageID'])) {
        $messageID = intval(base64_decode(trim($_GET['messageID'])));

        $isFavorited = Database::selectFirst("SELECT COUNT(*) FROM favorites WHERE user_id = ".intval($userID)." AND message_id = ".intval($messageID));
        $isSubscribed = Database::selectFirst("SELECT COUNT(*) FROM subscriptions WHERE message_id = ".intval($messageID)." AND user_id = ".intval($userID));

        respond(array(
            'status' => 'ok',
            'isFavorited' => (isset($isFavorited['COUNT(*)']) && $isFavorited['COUNT(*)'] > 0),
            'isSubscribed' => (isset($isSubscribed['COUNT(*)']) && $isSubscribed['COUNT(*)'] > 0)
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