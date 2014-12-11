# Changelog

## Version 1.0.0

 * Initial release

## Version 1.1.0

 * If you're seeing messages that are not from your friends (or friends of your friends), you will now see the user's country instead of just "Worldwide".
 * You will now see notifications whenever you have new unread messages from your friends (not more than once a day).
 * We will notify you when several new friends of yours have started using the app (not more than once a day).
 * (The ContactsUpdater service is now running regularly (automatically scheduled) and it is prevented from running more than once a day.)
 * (The back key actions have been improved for Activity classes with more than one content tab.)
 * (Added the "AppRater" library in order to ask users politely to rate the app on Google Play.)
 * ("Block author" is now the last option in the "Report message/comment" menu, because it should only be used as a last resort. The more people ignore each other, the fewer entries will they have in their feed, which could cause isolation/frustration within the app's UX.)

## Version 1.2.0

 * The app has a new icon.
 * Reports (of messages and comments) have been improved.
 * Adding a message to your favorites is even easier now.
 * (The reporting system may now automatically delete messages/comments and ban the author temporarily if enough votes have asked for that.)
 * (On the details page for a message, the focus in the ActionBar menu is now concentrated on the "Add to favorites" button. This does only require one click from now on (before: 2 clicks) and the other options ("Send as picture" and "Report message") have been moved to the overflow menu to give them less attention.)

## Version 1.2.1

 * Translations have been updated and new translations have been added. Thanks for helping us with your contributions!
 * In the comments to a message, you can now see which comments have been written by you or the original author of the message. This has been shown with colors only before, but now with some short supportive texts as well.
 * If you add a message to your favorites or comment on it, the "favorites" count and the "comments" count will be updated immediately.

## Version 1.2.2

 * Notifications and synchronization are not set up again correctly when you had switched off your phone and then restart it. This requires the new permission "Run at startup".
 * Sharing messages as a picture (via email, Facebook, WhatsApp, etc.) does now work without accessing your device's SD card. Therefore, we were able to remove the permissions "Write external storage" and "Read external storage".
 * (The message score algorithm on the server-side has been updated.)
 * (When adding a message to one's favorite, the counter does now really update immediately.)

## Version 1.3.0

 * Notifications for unread comments have been added. Whenever you write a comment, you'll be automatically notified of follow-up comments by other users.
 * In addition to the automatic subscriptions, you can manually subscribe and unsubscribe from a message's comments.
 * You can now remove messages that you added to your personal favorites before.
 * (The option "Invite friends" has been moved to the "About" page, which can be accessed via "Add message" page.)
 * (The language restriction has been activated for all countries. This means that, from now on, all users will only see messages in their own language.)
 * (An automatic score updater (worker) has been added so that the scores are not only updated when new comments/favorites events appear.)

## Version 1.3.1

 * A bug has been fixed that caused the app to close whenever you left the "Unread comments" (new notifications) page.
 * The author's country is now shown on messages for the 100 largest countries of the world. Before, there was only support for 30 countries.
 * More recent messages are now showing up while still showing popular messages with lots of comments/favorites at the top.
 * When posting new messages, you will now automatically subscribe for new comments on your own posts, so that you will never miss anything. Of course, you can cancel those notifications at any time.
 * (The score formula has basically been "(comments+favorites)/relativeTime". This means that messages with comments/favorites would always show up above any new messages, now matter how old they are. This was because new messages always started with a score of 0 and thus users would not see new messages, but only the old ("boring") content. Now the formula is "(1+comments+favorites)/relativeTime" so that every message starts with a slightly positive score and thus above most older messages.)
 * (An overflow for unsigned integers in MySQL has been fixed for the score algorithm. This caused lots of messages not to be updated (because an Exception was thrown) and resulted in errors when posting comments or saving messages as favorites.)
 * (The appearance of the badge with the notifications count (in the ActionBar) has been approved. When there are no new notifications, the badge is now shown in a translucent white instead of the normal green which symbolizes "new content".)

## Version 1.4.0

 * In the comments, every author now has their unique icon. This allows you to see which comments have been posted by the same person. But that is the only thing you'll know. When reading the comments on another message, everybody will have a new icon again.
 * The popular messages in your language do now have their own section. Instead of "Feed" and "Favorites", you'll now find "Friends", "Popular" and "Favorites". This allows you to see the same messages in "Popular" while putting the focus on your friends' messages only in "Friends".
 * The list of messages will automatically refresh now when you open the app later again. So far, you always had to refresh the list manually by pulling down the "Pull to refresh" bar.

## Version 1.4.1

 * A new tab "Latest" has been added, showing you the most recent messages of users who write in your language.
 * The tab "Popular" now shows a better selection of messages that have drawn interest from users worldwide.
 * Reports of inappropritate messages/comments have been improved: There are new categories (reasons for your reporting) and we'll openly show content that has been deleted with an appropriate notice. Before, these pieces of content would just have disappeard.
 * (The author's country is now shown on messages for the 130 largest countries of the world. Before, there was only support for 100 countries.)
 * (There are now admin users for the reporting of messages/comments, who always have full reporting power, causing reported content to be banned immediately.)

## Version 1.4.2

 * New messages and comments are now shown immediately just as you have published them. Refreshing the list is not required anymore.
 * Subscriptions page (unread comments): Messages are now removed from the list immediately as soon as the new comments have been read. This enables you to see through all subscriptions more easily.
 * Popular messages: The selection of the most-liked messages has been improved to put a stronger focus on the popularity and less focus on the newness, now that there is "Latest messages" as well.
 * (SMS verification via Twilio has been added so that users may continue to use the app with a second installation when the auto-generated password has changed.)
 * (A large part of the codebase has been extracted to external libraries that are distributed under the Apache License 2.0 separately.)
 * (Information as to how users can contribute translations is now shown more prominently on the "About" page.)
 * (A configuration file has been added to the client-side code (just as with the server-side code) to have a single file just for data specific to environment and distribution.)
 * (The complete project has been prepared for release under an open-source license by improving the documentation and the structure of the code.)
 * (Message are now marked as subscribed immediately after posting a new comment. Before, a refresh by loading from the server again was necessary.)

## Version 1.5.0

 * The application's icon, both the home screen icon and the small notification icon, have been updated to feature a ghost silhouette.
 * (All .gitignore files have been combined into a single .gitignore file at the project's root.)
 * (Updated screenshots for the current version have been added, as well as a history of screenshots back to the first version of the app.)
 * (The internal demo/preview mode, which is primarily used for taking screenshots, has been improved.)
 * (The GNU GPL v3 license has been added to README and all Java source files to prepare the release as open-source software.)

## Version 1.5.1

 * In the comments, emoticons such as ":-)" are now automatically replaced with their corresponding graphical smileys.
 * For Java/Android and PHP developers: The app is now open-source under the GNU GPL v3 license: http://www.delight.im/dev/faceless
 * (The title of the window that appears after choosing "Invite friends" has been adjusted. Before, it was just "Invite friends" as well. But users have struggled to understand that they can choose from the variety of apps on their phone and pick one option that they prefer for sending an invitation to their friends. Thus, the window title is now "Invite friends via ...".)
 * (The input type "short message" has been added as a recommendation to software keyboards for the EditText views at "Write message" and "Write comment".)
 * (The launcher icon (and small notification icon) have been slightly adjusted again.)
 * (The LICENSE file for the GNU GPL v3 license has been added, a notice that the app is open-source has been inserted into the app description, and the project has finally been made available as open-source software under the GNU GPL v3 license.)

## Version 1.6.0

 * A "Settings" screen has been added: You can now change the language of the app, among other options.
 * When posting a message, you must now specify if it is appropritate for all ages or for adults only.
 * In the message lists, you will now only see "Adult content" if you enable this in the settings.
 * A subtle menu has been added to the bottom of the main screen. You'll find "Settings" and "Invite friends" there. It fades away as soon as you start scrolling.
 * (The new PreferenceActivity screen shows the custom language selection, the "Adult content" switch and a link to the "Help" Activity, which could therefore be removed from the "Add" page, where it had always been inappropriate, anyway.)
 * (In exchange for the bottom menu, the large (and annoying) "Do you want to invite friends?" box at the top of the main screen is gone now.)
 * (On the "Subscriptions" page, there is now no menu at all (in the ActionBar). This makes the whole screen layout much cleaner and provides a better overview. Afterwards, one does always return back to the main screen with the menu, so that was not necessary on the "Subscriptions" page at all.)

## Version 1.6.1

 * Messages that have a wrong age restriction can now be reported.
 * If your country is not displayed correctly, you can now change it manually in the app's settings.
 * (When posting a new message, the message hint ("example text") has been shortened: The three lines "Be kind. / Be positive. / Be responsible." have been merged into the single line "Be kind and positive." to make it more simpel and effective. Furthermore, the old phrase "Type here ..." has been replaced by the more expressive "Share a thought ..." in all languages now.)

## Version 1.7.0

 * Notifications for unread comments have been added. Until now, you had to open the app and check in the top-right corner if there are any news. From now on, we'll push a message to your notification bar. And you can even choose the frequency of notifications you receive.

## Version 1.7.1

 * The size of the small notification icon (which shows up in the notification bar) has been slightly decreased.
 * The height of the age level selection on the "Add" page has been sligthly increased, so that it is a little bit more conspicuous.
 * A bug with the user icons in a message's comments thread (identicons) where they won't show up on certain devices has been fixed. Thanks, Gareth!
 * (The introduction that is shown to new users has been shortened from 7 steps to 5 steps.)
 * (The app is now open-source in all parts, finally, i.e. not only the server-side code, but also the client-side code. It's available under the GNU GPL v3 license: http://www.delight.im/dev/faceless)

## Version 1.8.0

 * The app has been redesigned with a new color scheme and a new font for messages/comments
 * Important controls have been moved to the bottom so that you can access them more easily and quickly, e.g. for switching to the comments view or submitting a new comment
 * You can now see the number of comments for every message right from the main screen
 * (The app does now use a blue color scheme, remarkably with a light-blue ActionBar. This is to make the app more refreshing and friendly instead of being all over in monotonous grey.)
 * (For messages and comments, the Ubuntu Font is now used (as a custom font from the assets). This has also solved the problem of emoji not displaying correctly in messages.)
 * (Timestamps are now only shown for messages/comments when they are fresh, i.e. at least 36 hours old. On the main screen (and the lists of messages in the four branches), the timestamp information at the bottom (besides the degree) has been replaced by the comments count. This is to shift the focus away from freshness of content to quality of content and discussions that are interesting, regardless of their age. Furthermore, showing the comments count on the main screen provides more value and may increase engagement.)
 * (The demo data and settings have been adjusted so that they result in better screenshots.)
 * (The progress indication on the introductory pages has been adjusted to end with 100% (was off by one step).)
 * (When the SMS ("Messages" application) intent is started in order to send the confirmation SMS, the app is now closed automatically, so that the user may re-open it later. Before, the user would return from sending the SMS and see the old dialog asking for the verification again (which is then outdated).)
 * (The screenshots have been updated due to the major design changes.)

## Version 1.8.1

 * Browsing the app has become easier and more convenient. We have improved and simplified the navigation in all parts of the app.
 * For every message, the favorites count and the comments count are now accompanied by a description to avoid any misunderstandings.
 * (All navigation bars (i.e. the ActionBar, the ButtonBar at the top, if available, and the ButtonBar at the bottom, if available) have been optimized to feature the critical features more prominently, and if possible, in the ButtonBar at the bottom. Examples include the "Write message" button which has been moved from the ActionBar at the top to the ButtonBar at the bottom where it is now the only option available (large touch area), the switch between "Message" and "Comments" on the details pages, and the form for submitting new comments on the details pages.)
 * (For the message properties "Comments" and "Favorites", descriptive texts have been added. Those are displayed on top of the message Views as well now, instead of just showing the number next to the property icon.)
 * (The pages "About", "Invite friends", the introduction for new users and the manual setup page are now using random colored backgrounds with patterns, analogous to the message Views.)
 * (The step size of the friends count (which is queried for notifications to the user themself) has been reduced from `5` to `3`, so that users receive updates about their friends arriving on Faceless more often and earlier (before quitting again?).)
 * (The API requests have been fixed on the client-side to support languages such as Hindi. Those languages did always throw errors and were not able to communicate with the API until now.)
 * (The "Invite friends" feature has been moved to its own Activity, where the user can now see a preview of the message being sent. This does also allow for more customization and is a preparation for more advanced features related to invitations.)
 * (The "Customize" action on the "Write message" page is now accompanied with a cog/gear icon (which symbolizes adjustments) instead of the image/picture icon (which caused misunderstandings and made users think its about photos that you can post).)
 * (The appearance of the Spinner "Age restriction" on the "Write message" page has been improved, where it is shown on a dark background and therefore needed white controls (which are now there).)
 * (The favorites count is now hidden when it's zero.)

## Version 1.9.0

 * Private messaging has been added: You can now reply to any comment privately by pressing the comment that you want to respond to and then choosing "Reply privately".
 * (Message texts do now fade in slowly for a more pleasant and intriguing experience.)
 * (The message degrees (the relationship distance to the author) are now shown on tabs "Latest" and "Popular" as well. So far, they had only been included in tabs "Friends" and "Favorites", and otherwise defaulted to "3" (country name / worldwide).)
 * (Old messages are now automatically wiped after a defined timeout (for now: 28 days).)
 * (A bug has been fixed where you could report your own comments (Thanks, atom from HackerOne!). This had been handled correctly on the server, anyway, but the client still offered those options and could thus cause confusion.)
 * (Messages/Comments reported by administrators do not cause actual reports to be filed anymore. This means that administrators can now use the reporting feature to simply delete inappropriate content without causing the authors to be blocked.)
 * (The API pages now have a public index page with some descriptive texts and the most important links for this project.)
 * (The screenshots for the Android app have been updated.)

## Version 1.9.1

 * "Latest" is now the default tab, followed by "Popular" and then "Friends". This is to ensure you always get the most recent and fresh messages from the community.
 * Private replies are now displayed much more clearly: Your avatar (user icon) is now shown next to the person you sent the reply to or received it from.
 * Your anonymous avatar (or user icon) is now visible as soon as you've posted your new comment. Before, you always had to refresh the comments thread in order to see your avatar.
 * (Messages with an age restriction of "Adults only" are now subtly marked as such: Instead of the normal symbol that is shown next to your relationship to the author, they do now feature a filled icon to convey the different age rating.)
 * (New messages are now becoming visible no later than 15 minutes after posting. Before, it took 0-60 minutes for them to become available.)
 * (Filtering out comments that have been deleted or that are private to another user is now done by MySQL instead of PHP. This does not only speed things up but it does also ensure that there is (almost) always a full page of comments returned, instead of a list much-shortened due to filtering.)
 * (The MaxClients setting for Apache on Heroku, which controls concurrent connections (processes) in PHP, has been increased from 1 (!) to 8.)
 * (Login throttling has been enabled on a per-user basis.)

## Version 1.10.0

 * Topics/categories have been added for messages, which you can filter by in the app's settings. This enables you to hide what you're not interested in and show more often what you like.
 * The old age restriction (none / adults only) has been removed. This kind of content filtering can now be easily achieved by selecting appropriate topics/categories.

## Version 2.0.0

 * The app's icon has been re-designed. We hope you like it :)
 * Old messages are now destroyed only after some time of inactivity. This means you will never lose a message anymore that you (and others) are still commenting on.
 * Each message's topic is now shown on the details page, in place of the favorites count. To save space and only show the most relevant information, the favorites count or the time is now shown in one place, depending on what's more relevant and expressive.
 * A bug has been fixed where messages in the category "About Faceless" would not be visible and the topic property would be displayed incorrectly on the details page.
 * (The launcher icon and the small notification icon have been re-designed by Ahmed Krishna (via oDesk). Instead of the poor drawing of a white ghost, it does now feature a well-designed masked person. The small notification icon is the mask only.)
 * (The app's description has been re-written from scratch. This is to adjust it to the experiences and changes that have come in the early stage of the app, and move the focus from the word "anonymity" and sharing with friends to talking freely and openly in general.)
 * (The "Invite friends" feature has been revamped completely and is now randomly linked to from all message feeds. This is done in the style of a normal message to make the promotion/incitement as natural as possible. Most importantly, a description/introduction to the process has been added, as well as an explanation as to why users gain by inviting their friends.)
 * (The sub menu that features the essential "Write message" button is now shown always (initially). Before, it would not have been shown when the list was empty or exactly one page long.)
 * (Example messages are now shown for all tabs/modes when they do not contain enough messages, not only for "Friends" as before.)
 * (The two entries "Invite friends" and "Help" in the settings do now have a description as well. This is for consistency with the other entries and for more clarity.)
 * (The SMS verification code is now more solid and expressive as the verification code has been wrapped in the middle of two descriptive text components.)
 * (When publishing a new comment, the comments count in the app is only increased immediately (without querying from the server again) when the comment was a public comment, not a private reply.)
 * (When writing a new message, a warning is now shown when the message text is still empty or no topic has been selected yet.)
 * (The KeyValueSpinner, AutoListPreference and other components and methods have been extracted to external libraries.)

## Version 2.0.1

 * (A bug has been fixed where the app would always crash at start-up if there is either no Internet connection or a server error.)
 * (The screen "Unread comments" (subscriptions) did include example messages (if too few items) and promotional messages (if enough items), just as the other screens, although it should not. The subscriptions feed must only show the few items where there has been new activity by other users, and thus the examples and promotional messages have been removed.)
 * (Updated screenshots for the current version have been added.)
 * (The in-app translation hint, which tells the user how they can contribute, has been adjusted slightly. In addition to that, the URL for the Localize platform has been updated to the new domain.)
 * (Facebook ad resources have been added to the graphics folder.)
 * (In demo mode (for taking screenshots or demonstrating how the app works), the screen "Write message" does now always use the same background pattern and color for consistency.)

## Version 2.1.0

 * New user icons/avatars: In the comments threads, you'll now find new icons for each user. Instead of the old geometrical forms you will now see cool real-world icons that you can use to address other users.
 * We've further improved the security of the app against people who get physical access to your phone. The app does now protect your account from being stolen by people who connect your phone to a computer via USB and try to copy all data via the backup mechanism.

## Version 2.2.0

 * The app has got a new icon (again) -- we hope it looks more friendly now
 * The details/comments page for single messages is now loading faster
 * A small space/divider in dark color has been added between the single message cards to improve the clarity and overall appearance
 * (The URL for the project's bug bounty program has been added to the HTTP headers (as "X-Bug-Bounty") so that people who examine the headers always know where they can report weaknesses)
 * (The details/comments page for single messages isn't blocking anymore. The user doesn't need to wait for the message details (is favorite? is subscribed?) to load anymore. Instead, the details are now loaded asynchronously, indicated by a loading/progress bar in the ActionBar only.)

## Version 2.3.0

 * The main screen is now even simpler and more convenient to use than before.
 * You can now write longer comments without breaking them up into small pieces.
 * (The "Popular" mode has been removed. In place of the "Popular" button in the tabs bar, there is now a small link to the settings.)
 * (As the "Settings" button has been moved from the ActionBar to the tabs bar, the "Add message" button could now be moved from the sub menu at the bottom to the ActionBar. The sub menu at the bottom has been removed.)
 * (The small notification icon has been updated to properly match the latest launcher icon.)
 * (The screenshots have been updated to reflect the UI changes.)

## Version 2.3.1

 * Links and email addresses are now clickable in messages and comments.
 * The emoji support has been improved and bugs (e.g. emoticons in links) have been fixed.
 * You can now share links and pieces of text from other apps with the "Share" option and send them to Faceless directly.

## Version 2.3.2

 * You are now prevented from accidentally writing a public comment when you actually wanted to write a private reply.
 * (The launcher icon has been updated again to be even more friendly and trustworthy.)
 * (The SMS backend has been changed from a US local number to a Swedish mobile number, which hopefully increases the reliability of the backend receiving SMS for verification purposes.)
 * (When on the "Friends" or "Favorites" tabs (or when viewing notifications), users will now see messages for all topics, which is a must because they may miss friends' posts otherwise or don't see messages with their own activity anymore.)
 * (An internal backend for phone-to-ID conversion has been added to simplify manual account lookups for support reasons in the future.)
 * (There will be no example messages on the "Favorites" tab anymore because this is not a live feed but a personally curated collection.)

## Version 2.4.0

 * The app is now saving up to 70% data usage, running faster and more stable on slow mobile data connections.
 * Notifications are now showing up more regular. If you're not receiving them fast enough, please check your settings.
 * (The server has been moved to Heroku's new official PHP support with PHP 5.4.14 and OPcache enabled.)
 * (Optional GZIP-compression for server responses has been added. The client software may request the server to compress its JSON output.)
 * (A linked support email address has been added to the SMS verification screen. Before, there was no way to get any help in case the verification process didn't work properly.)
 * (The app is now more responsive during the setup process. When the user ID and password are generated and then saved in the last step, the app did this in the UI thread before, causing the screen transition to freeze for 1-2 seconds. Now, the setup is pre-run on the last page of the introduction with a loading indication, without blocking the UI thread.)
 * (The highest frequency for the notifications has been renamed to "Normal", turning the other levels into "Rarely" and "Very rarely".)
 * (The highest frequency for the notifications is now the default leve, instead of the lowest frequency (apart from "Off") as before.)
 * (Protection against tap jacking (click jacking) has been added.)

## Version 2.4.1

 * Support for emoji (emoticons) has been improved on older devices.
 * (Before Android 4.1, there was no emoji font (neither AndroidEmoji.ttf nor NotoColorEmoji.ttf). This is why emoji could not be displayed for all users with Android 4.0 (ca. 8%) and were only shown as empty squares. For those older Android versions, emoji are now automatically reverted back to emoticons in text format.)

## Version 2.5.0

 * The category selection has been simplified and provides more clarity to the main screen now.
 * An improved "Popular" section is back! This allows you to find exciting topics more quickly, without reading all the messages from "Latest".
 * (The mode tabs (3-4, not really enough place for the 4th tab) have been replaced with a more convenient Spinner view. This allowed for a return of the "Popular" mode as there is now enough place for any number of sections.)
 * (The score algorithm for the "Popular" mode now gives more weight to the popularity (by saved favorites and comments) and less weight to the age of the message. This is to enhance the differentiation between "Latest" and "Popular".)

## Version 2.5.1

 * Translations have been updated for Spanish, Japanese, Turkish and Chinese.
 * (Timing attacks in hash comparisons on the server have been mitigated.)
 * (The cipher for the server-side encryption of messages and comments has been changed from Blowfish to AES.)
 * (Project has been made public on GitHub.)
 * (The three latest screenshots have been included in the project's README as a preview.)
 * (The latest release (APK) is now available for download via GitHub (right from the repository) as well.)
 * (Known issues and a list of similar services have been added to the proejct's README.)
 * (The database structure has been optimized.)

## Version 2.5.2

 * Emoji (emoticons) are now working correctly on all Android versions, even if not supported by Android in the first place.
 * Translations have been updated for Turkish and Chinese.
 * (The smooth scrollbar has been replaced with a step-based (skipping) scrollbar on the comments pages. This is because the items are not of equal height and thus the scrollbar was always changing its size while scrolling.)
 * (The mode selection (Spinner) is now correctly updated when pressing the back key on the main page.)

## Version 2.6.0

 * You can now decide who to share your messages with. This allows for completely anonymous messages that won't appear in your friends' feeds.
 * The process of posting a new message has been simplified and split to two separate screens for more clarity.
 * (The artificial delay when posting new messages has been removed to avoid confusion.)
 * (Refactored and restructured the configuration file for the Android client.)
 * (Refactored and restructured the configuration file for the server.)
 * (Added full documentation to the configuration file of the Android client.)
 * (Added full documentation to the configuration file of the server.)
 * (Let admin accounts post content as official messages.)
 * (Make HTTPS configurable so that development servers don't necessarily need SSL/TLS.)

## Version 2.6.1

 * Translations have been updated for Portuguese.
 * (Legacy support for the Blowfish cipher has been removed.)

## Version 2.7.0

 * The existing list of 19 topics has been extended by adding 15 new topics. These are available when posting new messages or filtering the personal feed.
 * The 14 background patterns have been replaced with 32 new background patterns of higher quality.
 * A bug has been fixed where the favorites count would not be decreased on deletion of a favorite. This caused the issue that you could infinitely increase the favorites count by adding/removing a message to/from your favorites.
 * (Added an optional conversation review feature for private threads. This may be responsibly used by service operators for anti-abuse and support reasons.)

## Version 2.7.1

 * Recommending the app by sharing an app link with friends has been simplified. All intermediate steps have been removed so that sharing is faster and more straightforward now. Furthermore, only SMS and messaging apps are now offered as means of sending the invitations, as these services are coupled to the address book just as this project is.
 * A certain number of friends who are active on this app is now required if users want to see their (friends of) friends' messages. Until they reach that threshold, the messages are hidden (checked on the client only as this is not relevant to security) and asked to invite more friends.

## Version 2.8.0

 * Location-based services have been added.
 * You can now add your *approximate* location (blurred to protect your privacy) to messages and share them with people nearby.
 * See how close (or far) you are to the author of a message you're just reading. Again, the distance is a rough approximation only to protect the privacy -- both yours and the author's.
 * Multi-line comments are now possible and the input field expands seamlessly for your convenience.

## Version 2.9.0

 * "Nearby" messages have been added -- so that you can see what people around you are talking about.
