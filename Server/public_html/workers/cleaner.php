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

$daysUntilDestruction = intval(CONFIG_MESSAGES_SELF_DESTRUCT_TIMEOUT);
if ($daysUntilDestruction < 1) {
    $daysUntilDestruction = 1;
}

// define a timeout and delete all content that has been created before that timestamp
$timeout = time() - (3600 * 24 * $daysUntilDestruction);
// delete old messages
Database::delete("DELETE FROM messages WHERE time_active < ".intval($timeout));
// delete old comments
Database::delete("DELETE FROM comments WHERE time_inserted < ".intval($timeout));

?>