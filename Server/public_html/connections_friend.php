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
    if (isset($_POST['userList'])) {
        // prevent users from uploading a contact list with more than 2,000 entries (roughly)
        if (strlen($_POST['userList']) < 90000) {
            // first prepare the lists of usernames
            $contactUsers = explode(',', $_POST['userList']);
            $sqlInsertValueList = "";
            $sqlWhereInList = "";
            $counter = 0;
            foreach ($contactUsers as $contactUser) {
                $usernameEscaped = Database::escape(makeHash($contactUser));
                // for all but the first element
                if ($counter > 0) {
                    // add a comma as the separator
                    $sqlInsertValueList .= ",";
                    $sqlWhereInList .= ",";
                }
                $sqlInsertValueList .= "(".$usernameEscaped.")";
                $sqlWhereInList .= $usernameEscaped;
                $counter++;
            }

            // if there were contacts in the list
            if ($counter > 0) {
                // first create dummy user elements for the contacts so that we have an ID
                Database::insert("INSERT IGNORE INTO users (username) VALUES ".$sqlInsertValueList);

                // then insert the new connections in both directions
                Database::insert("INSERT IGNORE INTO connections (from_user, type, to_user, time_inserted) SELECT ".intval($userID)." AS from_user, 'friend' AS type, id AS to_user, ".time()." AS time_inserted FROM users WHERE username IN (".$sqlWhereInList.") AND id != ".intval($userID));
                Database::insert("INSERT IGNORE INTO connections (from_user, type, to_user, time_inserted) SELECT id AS from_user, 'friend' AS type, ".intval($userID)." AS to_user, ".time()." AS time_inserted FROM users WHERE username IN (".$sqlWhereInList.") AND id != ".intval($userID));
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