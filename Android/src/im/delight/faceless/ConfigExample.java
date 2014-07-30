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

}
