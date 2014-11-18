<?php

require_once(__DIR__.'/../../base.php');

class Database {

    /**
     * PDO database object that is used internally to communicate with the DB
     *
     * @var PDO
     */
    protected static $db;

    public static function init($connectString, $username, $password) {
        try {
            self::$db = new PDO($connectString, $username, $password);
            self::$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        }
        catch (Exception $e) {
            throw new Exception('Could not connect to database');
        }
    }

    public static function escape($text) {
        return self::$db->quote($text);
    }

    public static function getLastInsertID() {
        return self::$db->lastInsertId(NULL);
    }

    public static function select($sql_string) {
        return self::$db->query($sql_string)->fetchAll();
    }

    public static function selectFirst($sql_string) {
        return self::$db->query($sql_string)->fetch();
    }

	/**
	 * Inserts a new row into the database using the given SQL statement
	 *
     * @param string $sql_string the SQL command to execute
	 * @return boolean whether the insert was successful (true) or not (false)
	 */
    public static function insert($sql_string) {
        try {
			self::$db->exec($sql_string);
			return true;
		}
		catch (Exception $e) {
			return false;
		}
    }

    public static function update($sql_string) {
        self::$db->exec($sql_string);
    }

    /**
     * Deletes rows from the database using the given SQL statement
     *
     * @param string $sql_string the SQL command to execute
     * @return int the number of affected (i.e. deleted) rows
     */
    public static function delete($sql_string) {
        return self::$db->exec($sql_string);
    }

}