package im.delight.faceless;

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

public class ConfigExample {
	
	/** Whether demo/preview mode should be enabled or not (must always be <false> before publishing a new release) */
	public static final boolean DEMO_ACTIVE = false;
	public static final String API_BASE_URL = "https://faceless-api.herokuapp.com";
	public static final String CLIENT_VERSION_PREFIX = "Faceless-Android-";
	public static final String SHARE_URL = "REPLACE_THIS_WITH_VALUE";
	public static final int PASSWORD_LENGTH_FACTOR = 3;
	public static final int UUID_LENGTH_IN_CHARS = 36;
	public static final String CRYPTO_HASH_SEED_ROT13 = "REPLACE_THIS_WITH_VALUE";
	public static final String CRYPTO_HMAC_KEY_ROT13 = "REPLACE_THIS_WITH_VALUE";
	public static final int CRYPTO_HASH_ITERATIONS = 4;
	public static final String SUPPORT_EMAIL = "info@example.org";
	
	/** This class may not be instantiated */
	private ConfigExample() { }
	
	public static class Preferences {

		public static String USERNAME = "username";
		public static String PASSWORD = "password";
		public static String REGION_CODE = "region_code";
		public static String INTRO_STEP = "intro_step";
		public static String LAST_TIME_CONTACTS_UPDATER = "last_time_contacts_updater";
		public static String LAST_TIME_GENERAL_NOTIFICATIONS = "last_time_notifications_sender";
		public static String LAST_TIME_SUBSCRIPTION_NOTIFICATIONS = "last_time_subscription_notifications";
		public static String SUBSCRIPTION_NOTIFICATIONS_FREQUENCY = "subscription_notifications";
		public static String FRIENDS_COUNT = "friends_count";
		public static String LATEST_MESSAGE_READ = "latest_message_read";
		
		/** This class may not be instantiated */
		private Preferences() { }

	}

}
