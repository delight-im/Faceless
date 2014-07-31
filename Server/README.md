# Server

 * Live: https://faceless-api.herokuapp.com/
 * PHP >= 5.3.27

## HTTP Basic Auth

HTTP Basic Auth is required for all requests.

## User-Agent

A `User-Agent` header of the form `Faceless-<PLATFORM>-<VERSION>` must always be provided.

`<PLATFORM>` is the name of the client's platform and will be parsed as a string.

`<VERSION>` is the incremental version ID of the client software and will be parsed as an integer.

## Signature header

All requests must include a HTTP header called `HTTP_X_METHOD_SIGNATURE` that contains a valid HMAC SHA-256 signature (Base64) for the specific API method.

## Response formats

The response format is always JSON.

## API Status Responses

 * `ok`
 * `maintenance`
 * `bad_request`
 * `outdated_client`
 * `not_authorized`

## Message degrees

The degree is an integer and may be `0` (oneself), `1` (direct friend), `2` (friend of a friend) or above (geographical origin will be shown).

## API Methods

### `GET /messages/list`
 * Retrieves 50 messages from from the server. This means that either the table of access tokens or the table of favorites is joined with the table of messages. The messages are loaded from the database and then decrypted on the server before they are returned. If there are not enough (new) messages on the server for the authenticating user, some popular messages for the user's language may be returned as well.
 * Request:
   * `mode` : `enum` : either `friends`, `popular`, `latest`, `favorites` or `subscriptions`
   * `page` : `int` : the page ID to return (starting with 0)
   * `topicsList` : `string` : the list (CSV) of topics that messages are to be returned for
   * `languageISO3` (optional) : `string` : the three-letter ISO 639-2/T code for the language that popular messages should be retrieved for
 * Response:
   * `status` : `string` : the API status code as described above
   * `messages` (optional) : `array` : list of messages
     * `id` : `string` : unique ID (Base64) of the message
     * `degree` : `int` : the degree of the message as described above
     * `colorHex` : `string` : the background color (#RRGGBB)
	 * `patternID` : `int` : the background pattern ID
     * `text` : `string` : the message text
	 * `topic` : `enum` : the topic of the message
     * `favoritesCount` : `int` : the number of times the message has been favorited
     * `commentsCount` : `int` : the number of times the message has been commented on
	 * `countryISO3` : 'string' : the three-letter ISO 3166-1 code for the country of origin
     * `time` : `int` : publishing date of the message in seconds since January 1, 1970
   * `subscriptionUpdates` (optional) : `int` : the number of new subscription updates or `-1`

### `POST /messages/details`
 * Retrieves details about the given message from the server. This includes whether the message has been favorited by the authenticating user and whether the authenticating user has subscribed to the comments.
 * Request:
   * `messageID` : `string` : unique ID (Base64) of the message
 * Response:
   * `status` : `string` : the API status code as described above
   * `isFavorited` (optional) : `boolean` : whether the message has been favorited by the authenticating user
   * `isSubscribed` (optional) : `boolean` : whether the authenticating user has subscribed to the comments thread of the message

### `POST /messages/new`
 * Publishes a new message to the server that will be dispatched to its receivers later (asynchronously). Dispatching will be done by creating entries in the recipients' feeds for each message. Access will initially be granted to all user that have a `friend` connection (as opposed to `block`) with the authenticating user, but may be extended by sharing.
 * Request:
   * `colorHex` : `string` : the background color (#RRGGBB)
   * `patternID` : `int` : the background pattern ID
   * `text` : `string` : the text of the message to publish
   * `topic` : `enum` : the topic of this message
   * `languageISO3` (optional) : `string` : the three-letter ISO 639-2/T code for the language this message is written in
   * `countryISO3` (optional) : `string` : the three-letter ISO 3166-1 code for the country this message is written from
   * `random` : `string` : UUID that has been randomly generated on the client and will be used as the basis for the message secret
 * Response:
   * `status` : `string` : the API status code as described above
   * `messageID` (optional) : `string` : unique ID (Base64) of the message
   * `messageTime` (optional) : `int` : publishing date of the message in seconds since January 1, 1970

### `GET /comments/list`
 * Retrieves the 100 latest comments for the given message.
 * Request:
   * `messageID` : `string` : unique ID (Base64) of the message
 * Response:
   * `status` : `string` : the API status code as described above
   * `comments` (optional) : `array` : a list of comments for the message
     * `id` : `string` : unique ID (Base64) of the comment
     * `text` : `string` : the text of the comment
	 * `privateRecipientInThread` : `string` : the unique ID of the private recipient of this comment that is only valid for the current message's comments thread, if any, or `null`
     * `isOwner` : `boolean` : whether the user who posted the comment is also the owner of the message commented on
	 * `isSelf` : `boolean` : whether the user who posted the comment is the authenticating user themself
	 * `ownerInThread` : `string` : the unique ID of the owner of this comment that is only valid for the current message's comments thread
     * `time` : `int` : writing date of the comment in seconds since January 1, 1970

### `POST /comments/new`
 * Publishes a new comment for the given message. This does also update the message's score (overall popularity). The authenticating user will automatically subscribe to the comments thread and all other subscribers will be notified that there is a new comment.
 * Request:
   * `messageID` : `string` : unique ID (Base64) of the message
   * `privateReplyToComment` (optional) : `string` : unique ID (Base64) of the user who this comment is shared privately with (if any)
   * `text` : `string` : the text of the comment to publish
   * `random` : `string` : UUID that has been randomly generated on the client and will be used as the basis for the message secret
 * Response:
   * `status` : `string` : the API status code as described above
   * `commentID` (optional) : `string` : unique ID (Base64) of the comment
   * `commentTime` (optional) : `int` : writing date of the comment in seconds since January 1, 1970
   * `ownerInThread` (optional) : `string` : the unique ID of the owner of this comment that is only valid for the current message's comments thread
   * `privateRecipientInThread` (optional) : `string` : the unique ID of the private recipient of this comment that is only valid for the current message's comments thread, if any, or `null`

### `POST /favorites/set`
 * Adds/removes the given message to/from the authenticating user's list of favorites. The message will then be at the top of the favorites list, as the most recent favorites are returned first. If the given message was already in the authenticating user's list of favorites, it is pushed back to the top. If it did not exist yet, the favorites count of the message is increased by one. This does also update the message's score (overall popularity). Access to the message is granted to all friends of the authenticating user, increasing the degree for them by one.
 * Request:
   * `messageID` : `string` : unique ID (Base64) of the message
   * `favorited` : `int` : `1` if the message is to be added, `0` if the message is to be removed
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /subscriptions/set`
 * Adds/removes the message to/from the authenticating user's list of subscriptions regarding new comments.
 * Request:
   * `messageID` : `string` : unique ID (Base64) of the message
   * `subscribed` : `int` : `1` if the message is to be added, `0` if the message is to be removed
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /subscriptions/clear`
 * Clears all unread subscription updates for the authenticating user.
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /connections/friend`
 * Adds a new `friend` connection between the authenticating user and all users from the supplied list. The `friend` connection is bidirectional by default, i.e. when this method is called for one user, both users will have each other in their friend list. All existing connections of the same type will be replaced with only the new ones. A `friend` connection will, however, not be established when there is an existing connection of the type `block` already.
 * Request:
   * `userList` : `string` : a comma-separated list of usernames to whom a "friend" connection is to be added (max 90,000 chars)
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /connections/friend/count`
 * Adds a new `friend` connection between the authenticating user and all users from the supplied list. The `friend` connection is bidirectional by default, i.e. when this method is called for one user, both users will have each other in their friend list. All existing connections of the same type will be replaced with only the new ones. A `friend` connection will, however, not be established when there is an existing connection of the type `block` already.
 * Response:
   * `status` : `string` : the API status code as described above
   * `friends` (optional) : `int` : the number of friends (in steps of 5) that the user has on the platform

### `POST /connections/block`
 * Adds a new `block` connection between the authenticating user and the author of the supplied message. The `block` connection is unidirectional. Existing connections of the same type will not be replaced but new ones will be added in addition. A `block` connection will always be enforced and does also overwrite existing `friend` connections.
 * Request:
   * `contentType` : `enum` : either `message` or `comment`
   * `contentID` : `string` : unique ID (Base64) of the message/comment whose auther should be blocked
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /reports/new`
 * Reports a message as inappropriate.
 * Request:
   * `contentType` : `enum` : either `message` or `comment`
   * `contentID` : `string` : unique ID (Base64) of the message/comment which should be reported
   * `reason` : `int` : the reason (constant) why this message has been flagged
 * Response:
   * `status` : `string` : the API status code as described above

### `POST /verifications/prepare`
 * Prepares a verification text message by generating a secret code and saving the new password to set for the user. The user may sent a text message with the returned code to the returned phone number to activate their account and reset the password. The text message sent by the user is currently handled by Twilio which sends a POST request to `/internal/sms_verify.php`.
 * Authentication: Authentication must be attempted as always, but instead of the valid password, the desired new password must be set.
 * Response:
   * `status` : `string` : the API status code as described above
   * `apiPhoneNumber` : `string` : the phone number that is controlled by this API and which the user may send their verification code to
   * `verificationCode` : `string` : the verification code that the user must send in order to reset their password
   * `validUntil` : `int` : the expiry date of this verification request in seconds since January 1, 1970