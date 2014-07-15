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

require_once(__DIR__.'/../../base.php');

// initialize the database connection
try {
    Database::init(CONFIG_DB_CONNECT_STRING, CONFIG_DB_USERNAME, CONFIG_DB_PASSWORD);
}
catch (Exception $e) {
    throw new Exception('Could not connect to database');
}

$messagesToDispatch = Database::select("SELECT id, user_id FROM messages WHERE dispatched = 0 AND time_published < ".time()." LIMIT 0, 250");
$dispatched = array();
foreach ($messagesToDispatch as $messageToDispatch) {
    Database::insert("INSERT INTO feeds (user_id, message_id, degree) SELECT from_user AS user_id, ".intval($messageToDispatch['id'])." AS message_id, 1 AS degree FROM connections WHERE to_user = ".intval($messageToDispatch['user_id'])." AND type = 'friend' ON DUPLICATE KEY UPDATE degree = IF(VALUES(degree) < degree, VALUES(degree), degree)");
    $dispatched[] = intval($messageToDispatch['id']);
}

if (count($dispatched) > 0) {
    Database::update("UPDATE messages SET dispatched = 1 WHERE id IN (".implode(",", $dispatched).")");
}

?>