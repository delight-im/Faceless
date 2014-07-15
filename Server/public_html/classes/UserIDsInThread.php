<?php

require_once(__DIR__.'/Database.php');

/** Exposes access to users' public IDs that are unique only within a certain scope */
class UserIDsInThread {

    /**
     * Creates a new public ID for the given user that is valid only in the given comments thread
     *
     * @param int $messageID the ID of the message to create the user's public ID for
     * @param int $userID the private ID of the user to create the public ID for
     */
    public static function create($messageID, $userID) {
        Database::insert("INSERT IGNORE INTO ids_in_threads (content_type, content_id, private_id, public_id) SELECT 'message', ".intval($messageID).", ".intval($userID).", (COALESCE(MAX(public_id), 0)+1) FROM ids_in_threads WHERE content_type = 'message' AND content_id = ".intval($messageID));
    }

    /**
     * Returns a user's public ID by looking up the given user's private ID
     *
     * @param int $messageID the ID of the message to get data for
     * @param int $userID the private ID of the user to get the public ID for
     * @return string the user's public ID or NULL
     */
    public static function getByUser($messageID, $userID) {
        $res = Database::selectFirst("SELECT public_id FROM ids_in_threads WHERE content_type = 'message' AND content_id = ".intval($messageID)." AND private_id = ".intval($userID));
        if (isset($res['public_id'])) {
            return $res['public_id'];
        }
        else {
            return NULL;
        }
    }

    /**
     * Returns a mapping of all users' private IDs to their public IDs for the given comments thread
     *
     * @param int $messageID the ID of the message to get data for
     * @return array the associative array mapping users' private IDs (keys) to their public IDs (values)
     */
    public static function get($messageID) {
        $out = array();
        $users = Database::select("SELECT private_id, public_id FROM ids_in_threads WHERE content_type = 'message' AND content_id = ".intval($messageID));
        foreach ($users as $user) {
            $out[$user['private_id']] = $user['public_id'];
        }
        return $out;
    }

    /**
     * Returns a user's private and public ID by looking up the given comment's author
     *
     * @param int $messageID the ID of the message to get data for
     * @param int $commentID the ID of the comment to get data for
     * @return array the result array containing the two fields 'user_id' and 'public_id'
     */
    public static function getByComment($messageID, $commentID) {
        return Database::selectFirst("SELECT a.user_id, b.public_id FROM comments AS a LEFT JOIN ids_in_threads AS b ON b.content_type = 'message' AND b.content_id = ".intval($messageID)." AND b.private_id = a.user_id WHERE a.id = ".intval($commentID));
    }

}

?>