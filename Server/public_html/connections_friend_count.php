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

    // get all friends who have used the application at least once
    $friends = Database::select("SELECT a.to_user FROM connections AS a JOIN users AS b ON a.to_user = b.id WHERE a.from_user = ".intval($userID)." AND b.time_registered IS NOT NULL");

    // prepare the active friends counter
    $friendsActive = 0;

    // iterate over list of friends
    foreach ($friends as $friend) {
        // if the friend has a valid user ID and is not the authenticating user themself
        if (isset($friend['to_user']) && $friend['to_user'] != $userID) {
            // increase the counter by one
            $friendsActive++;
        }
    }

    // take away some precision from the count by grouping numbers in units
    $friendsActive = ((int) ($friendsActive / CONFIG_FRIEND_COUNT_STEP_SIZE)) * CONFIG_FRIEND_COUNT_STEP_SIZE;

    respond(array(
        'status' => 'ok',
        'friends' => $friendsActive
    ));
}
else {
    respond(array('status' => 'bad_request'));
}

?>