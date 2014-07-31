<?php

define('CONFIG_DB_CONNECT_STRING', 'REPLACE_THIS_WITH_VALUE'); // PDO database connect string (e.g. mysql:host=example.com;dbname=example)
define('CONFIG_DB_USERNAME', 'REPLACE_THIS_WITH_VALUE'); // username for database authorization
define('CONFIG_DB_PASSWORD', 'REPLACE_THIS_WITH_VALUE'); // password for database authorization
define('CONFIG_API_LIVE', true); // whether the service is live or down due to maintenance
define('CONFIG_API_DEBUG', true); // whether the API is in debugging mode or not
define('CONFIG_ENFORCE_SSL', false); // whether to require HTTPS (SSL/TLS) for all requests (should be set to <true> in production environments)
define('CONFIG_API_CLIENTS', serialize(array('Android' => 21))); // a list of valid API clients with their respective minimum software version ID
define('CONFIG_API_SECRET', 'REPLACE_THIS_WITH_VALUE'); // secret used to verify request signatures (can be any strong password, e.g. [a-zA-Z0-9]{70,})
define('CONFIG_HEADER_SIGNATURE', 'HTTP_X_METHOD_SIGNATURE'); // the HTTP header field that contains the API method signature (HMAC)
define('CONFIG_HEADER_TIMESTAMP', 'HTTP_X_METHOD_TIMESTAMP'); // the HTTP header field that contains the API method timestamp
define('CONFIG_HMAC_ALGORITHM', 'sha256'); // the cryptographic hash algorithm that will be used for HMAC signatures
define('CONFIG_SERVER_SECRET', 'REPLACE_THIS_WITH_VALUE'); // secret used to encrypt messages before storing them in the database (can be any strong password, e.g. [a-zA-Z0-9]{70,})
define('CONFIG_MESSAGES_PER_PAGE', 50); // the number of messages to return per page
define('CONFIG_COMMENTS_PER_PAGE', 100); // the number of comments to return per page
define('CONFIG_MAX_MESSAGE_DELAY', 900); // the maximum delay in seconds to delay new messages with
define('CONFIG_ADMIN_USER_IDS', serialize(array())); // a list of user IDs that have administrator privileges
define('CONFIG_API_PHONE_NUMBER', 'REPLACE_THIS_WITH_VALUE'); // phone number that is part of this API for account verification (e.g. your SMS-enabled Twilio phone number)
define('CONFIG_TWILIO_AUTH_CODE', 'REPLACE_THIS_WITH_VALUE'); // secret Twilio API authentication code (get this from your Twilio account online)
define('CONFIG_CLIENT_HASH_ALGORITHM', 'sha256'); // hash algorithm used on client-side to hash phone numbers
define('CONFIG_CLIENT_HASH_ITERATIONS', 4); // number of hash iterations used on client-side to hash phone numbers
define('CONFIG_CLIENT_HASH_SEED', 'REPLACE_THIS_WITH_VALUE'); // seed used on client-side to hash phone numbers (can be any strong password, e.g. [a-zA-Z0-9]{24,})
define('CONFIG_FRIEND_COUNT_STEP_SIZE', 3); // the step size to use for the displayed friend count (should not be less than 3) in order to take away some precision and protect users' privacy)
define('CONFIG_MESSAGES_SELF_DESTRUCT_TIMEOUT', 28); // the number of days (>= 1) after which messages are to be wiped automatically
define('CONFIG_THROTTLING_LOGIN_ATTEMPTS', 100); // the maximum number of login attempts per user and day before throttling is activated
define('CONFIG_THROTTLING_LOGIN_HOURS', 12); // the number of hours to throttle a user's login for if throttling has been caused

?>