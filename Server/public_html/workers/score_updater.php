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

// list of languages to run the updater for (incomplete - must be updated regularly)
$languages = array(
    'DEU',
    'ENG',
    'NLD',
    'ITA',
    'POR',
    'SPA',
    'FRA',
    'RUS',
    'RON',
    'TUR',
    'CES',
    'SWE',
    'DAN',
    'VIE'
);

$language = $languages[array_rand($languages)];

Database::update("UPDATE messages SET score = ".getScoreUpdateSQL()." WHERE language_iso3 = ".Database::escape($language)." ORDER BY score DESC LIMIT 1000");

?>