# Faceless

Faceless is where you can talk freely. Share messages with your friends and the whole world.

## Preview ([Android](https://www.delight.im/get/faceless))

Main screen | Message details | Adding messages
:-------------------------:|:-------------------------:|:-------------------------:
<img src="Graphics/Screenshots/Version 2.9.0/01 Main.png?raw=true" alt="01 Main" width="200" /> | <img src="Graphics/Screenshots/Version 2.9.0/02 Details.png?raw=true" alt="02 Details" width="200" /> | <img src="Graphics/Screenshots/Version 2.9.0/03 Add.png?raw=true" alt="03 Add" width="200" />

## Download

 * Android: [Google Play](https://www.delight.im/get/faceless) or [GitHub](Releases/Android/Latest.apk?raw=true)

## Installation

### Android

 1. Rename [`Android/src/im/delight/faceless/ConfigExample.java`](Android/src/im/delight/faceless/ConfigExample.java) to `Config.java` and replace all occurrences of `REPLACE_THIS_WITH_VALUE` with a proper configuration value for your setup
    * For `CRYPTO_HASH_SEED_ROT13`, generate a long random string, e.g. `9QMfiOCPJ78gaRY6DZjzR7HQ`
    * For `CRYPTO_HMAC_KEY_ROT13`, generate a long random string as well, e.g. `7zKCg9pU2ulUgYTi8ImHZETQg5AtCP3UWNwrLI2QCwl8Aiil3jOUmh52HCHF29ssOezUMx4c`
 2. Add the following dependencies as library projects:
    * [Android-Countries](https://github.com/delight-im/Android-Countries)
    * [Android-Time](https://github.com/delight-im/Android-Time)
    * [ActionBar-PullToRefresh](https://github.com/chrisbanes/ActionBar-PullToRefresh) with [SmoothProgressBar](https://github.com/castorflex/SmoothProgressBar)

### Server

 1. Set up a web server with Apache + PHP + MySQL
 2. Optionally, set up a Twilio account and register an SMS-enabled phone number POSTing to the public URL of `Server/htdocs/internal/sms_verify.php`. If you skip this step, users will still be able to use the app, but only on a single device. When switching to a new device, they won't be able to use the app anymore. This is because re-activation on the new device is done via this SMS verification.
 3. Rename `Server/config.example.php` to `Server/config.php` and replace all occurrences of `REPLACE_THIS_WITH_VALUE` with a proper configuration value for your setup
    * For `CONFIG_CLIENT_HASH_SEED`, take the value of `CRYPTO_HASH_SEED_ROT13` from the Android configuration and encode it [using ROT13](http://www.rot13.com/)
    * For `CONFIG_API_SECRET`, take the value of `CRYPTO_HMAC_KEY_ROT13` from the Android configuration and encode it [using ROT13](http://www.rot13.com/)
    * If you haven't set up SSL/TLS for your server yet, disable `CONFIG_ENFORCE_SSL` by setting it to `false`
    * Set `CONFIG_API_DEBUG` to `true` while setting up and debugging your server instance
 4. Set up cron jobs for `htdocs/workers/dispatcher.php`, `htdocs/workers/score_updater.php` and `htdocs/workers/cleaner.php`
 5. Enable `mod_rewrite` for the Apache web server
 6. If you don't host your server on Heroku, you can delete the two files [`Server/Procfile`](Server/Procfile) and [`Server/web-boot.sh`](Server/web-boot.sh)

### Troubleshooting

 * If the app responds with "please check your internet connection"
   * Please copy the value of `API_BASE_URL` from your Android configuration and append `/messages/list` to it
   * Try to open that URL in your web browser
   * If the URL doesn't exist (`404 Not Found`), either your directory paths are wrong or `mod_rewrite` is not working correctly
   * If `mod_rewrite` is not working, please ask your hosting provider for help or check your `httpd.conf` for `LoadModule rewrite_module modules/mod_rewrite.so` and `AllowOverride FileInfo`
 * If the app responds that it is "out of service"
   * Please check that `CONFIG_API_LIVE` is set to `true` in your server configuration
   * Please check that the database configuration in the `CONFIG_DB_*` constants in your server configuration is correct
 * Optionally, if you want to debug the server responses in general
   * Please open [`Android/src/im/delight/faceless/Server.java`](Android/src/im/delight/faceless/Server.java) from the Android app
   * Search for the method `protected static int parseStatus(final String responseText, final boolean requireError)`
   * Add something like `System.out.println(responseText);` as the first line inside that method
   * All your server responses will now be logged to your console by the Android app

## Security

 * [Disclose bugs and vulnerabilities](https://www.delight.im/security/faceless) or read more about [security](SECURITY.md).

## Translations

 * Help translate by visiting [Faceless on Localize](https://www.localize.im/v/36)
 * View the full [list of translators](TRANSLATORS.md)

## Known issues

### Android

 * The app cannot be installed on devices without telephony features (e.g. tablets). This is because (a) the app is optimized for phones and (b) users would have to enter their phone number manually, otherwise, and (c) the app curently can run on a single device only.

### Twilio

 * "At the receiving end of SMS messages, Twilio cannot guarantee that every international SMS message sent to us will be received in your SMS log. Twilio accepts these messages, however it is the responsibility of the sending phone number carrier to deliver the SMS message to our network. I'd particularly recommend using our UK mobile numbers or Swedish mobile numbers. These numbers have been pretty successful at receiving SMS from other countries. There are two other things you should be aware of which affect the delivery of inbound international SMS: not all users will have plans which support international SMS, and international roaming might make the phone unable to send or receive SMS. If your users are primarily travelers, it's very likely that you will see issues of this type." (Lauren B. from Twilio)

## Similar services

 * [Secret](http://www.secret.ly/)
 * [Whisper](http://whisper.sh/)
 * [Yik Yak](http://yikyakapp.com/)
 * [Experience Project](http://www.experienceproject.com/)
 * [PostSecret](http://postsecret.com/)

## License

```
Copyright (C) 2014 www.delight.im <info@delight.im>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see {http://www.gnu.org/licenses/}.
```
