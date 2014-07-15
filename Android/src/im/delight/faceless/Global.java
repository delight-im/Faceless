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

import im.delight.android.baselib.Data;
import im.delight.android.baselib.Phone;
import im.delight.android.baselib.Strings;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.View;

public class Global {
	
	/** This class may not be instantiated */
	private Global() { }

	public static class Setup {
		
		/** This class may not be instantiated */
		private Setup() { }

		private static String mUsername;
		private static String mPassword;
		private static String mRegionCode;

		public synchronized static boolean isComplete() {
			return mUsername != null && mUsername.length() > 0 && mPassword != null && mPassword.length() > 0 && mRegionCode != null && mRegionCode.length() > 0;
		}
		
		public synchronized static String getUsername() {
			return mUsername;
		}
		
		public synchronized static String getPassword() {
			return mPassword;
		}
		
		public synchronized static String getRegionCode() {
			return mRegionCode;
		}
		
		public synchronized static void load(SharedPreferences prefs) {
			mUsername = prefs.getString(Config.Preferences.USERNAME, "");
			mPassword = prefs.getString(Config.Preferences.PASSWORD, "");
			mRegionCode = prefs.getString(Config.Preferences.REGION_CODE, "");
		}
		
		public synchronized static void save(SharedPreferences prefs) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Config.Preferences.USERNAME, mUsername);
			editor.putString(Config.Preferences.PASSWORD, mPassword);
			editor.putString(Config.Preferences.REGION_CODE, mRegionCode);
			editor.apply();
		}
		
		public synchronized static boolean runAuto(ContextWrapper context, SharedPreferences prefs) {
			String[] phoneData = getDevicePhoneNumber(context);
			if (phoneData != null) {
				String phoneNumber = phoneData[0];
				String phoneRegionCode = phoneData[1];
				if (phoneNumber != null && phoneNumber.length() > 0) {
					if (phoneRegionCode != null && phoneRegionCode.length() > 0) {
						mUsername = Crypto.hash(phoneNumber);
						mPassword = getRandomPassword();
						mRegionCode = phoneRegionCode;
						save(prefs);
						return true;
					}
					else {
						return false;
					}
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		
		public synchronized static void runManually(String[] validatedPhoneData, SharedPreferences prefs) {
			mUsername = Crypto.hash(validatedPhoneData[0]);
			mPassword = getRandomPassword();
			mRegionCode = validatedPhoneData[1];
			save(prefs);
		}
		
		private static String getRandomPassword() {
			StringBuilder out = new StringBuilder(Config.UUID_LENGTH_IN_CHARS * Config.PASSWORD_LENGTH_FACTOR);
			for (int i = 0; i < Config.PASSWORD_LENGTH_FACTOR; i++) {
				out.append(UUID.randomUUID().toString());
			}
			return out.toString();
		}

		private static String[] getDevicePhoneNumber(ContextWrapper context) {
			try {
				String countryIso2 = Phone.getCountry(context, "US");
				if (countryIso2 != null && countryIso2.length() == 2) {
					String phoneNumber = Phone.getNumber(context);
					if (phoneNumber != null && phoneNumber.length() > 0) {
						return PhoneWithLib.normalizeNumber(phoneNumber, countryIso2);
					}
				}
			}
			catch (Exception e) { }
			return null;
		}

	}
	
	public static class PhoneWithLib {
		
		/** This class may not be instantiated */
		private PhoneWithLib() { }
		
		public static String[] normalizeNumber(String phoneNumber, String defaultRegion) {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			PhoneNumber input;
			try {
				input = phoneUtil.parse(phoneNumber, defaultRegion.trim().toUpperCase(Locale.US));
				if (phoneUtil.isValidNumber(input)) {
					String parsedRegionCode = phoneUtil.getRegionCodeForNumber(input);
					if (parsedRegionCode != null) {
						return new String[] { phoneUtil.format(input, PhoneNumberUtil.PhoneNumberFormat.E164), parsedRegionCode };
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}
			catch (Exception e) {
				return null;
			}
		}

	}
	
	public static class Crypto {
		
		/** This class may not be instantiated */
		private Crypto() { }
		
		private static final String HASH_ALGORITHM = "SHA-256";
		private static final String HASH_CHARSET = "UTF-8";
		private static final String HMAC_ALGORITHM = "HmacSHA256";
		private static final String HMAC_CHARSET = "UTF-8";
		private static final int ENCODING_BASE64 = 1;
		private static final int ENCODING_HEX = 2;
		
		public static String hash(String input) {
			String output = "";
			String seed = Strings.rot13(Config.CRYPTO_HASH_SEED_ROT13);
			for (int i = 0; i < Config.CRYPTO_HASH_ITERATIONS; i++) {
				output = hashInternal(input+output+seed, ENCODING_BASE64);
			}
			return output;
		}
		
		private static String hashInternal(String input, int encoding) {
			try {
				MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
				md.update(input.getBytes(HASH_CHARSET), 0, input.length());
				byte[] result = md.digest();
				if (encoding == ENCODING_BASE64) {
					return Base64.encodeToString(result, Base64.NO_WRAP);
				}
				else if (encoding == ENCODING_HEX) {
					return Data.binToHex(result);
				}
				else {
					throw new RuntimeException("Unknown encoding: "+encoding);
				}
			}
			catch (Exception e) {
				return null;
			}
		}
		
		public static String sign(String input) {
			return signInternal(input, ENCODING_BASE64);
		}
		
		private static String signInternal(String input, int encoding) {
			try {
				SecretKeySpec keySpec = new SecretKeySpec(Strings.rot13(Config.CRYPTO_HMAC_KEY_ROT13).getBytes(HMAC_CHARSET), HMAC_ALGORITHM);

				Mac mac = Mac.getInstance(HMAC_ALGORITHM);
				mac.init(keySpec);

				byte[] result = mac.doFinal(input.getBytes(HMAC_CHARSET));
				if (encoding == ENCODING_BASE64) {
					return Base64.encodeToString(result, Base64.NO_WRAP);
				}
				else if (encoding == ENCODING_HEX) {
					return Data.binToHex(result);
				}
				else {
					throw new RuntimeException("Unknown encoding: "+encoding);
				}
			}
			catch (Exception e) {
				return null;
			}
		}

	}
	
	public static class MessagePropertyDrawables {

		private static final int COLOR_BLACK = 0;
		private static final int COLOR_WHITE = 1;
		private static final int TYPE_TIME = 0;
		private static final int TYPE_DEGREE = 1;
		private static final int TYPE_FAVORITES = 2;
		private static final int TYPE_COMMENTS = 3;
		private static final int TYPE_TOPIC = 4;
		private Drawable[][] mDrawables;
		
		public MessagePropertyDrawables(Context context) {
			Resources res = context.getResources();
			mDrawables = new Drawable[5][2];

			mDrawables[TYPE_TIME][COLOR_BLACK] = res.getDrawable(R.drawable.ic_sp_time_black);
			mDrawables[TYPE_TIME][COLOR_WHITE] = res.getDrawable(R.drawable.ic_sp_time_white);
			mDrawables[TYPE_DEGREE][COLOR_BLACK] = res.getDrawable(R.drawable.ic_sp_degree_black);
			mDrawables[TYPE_DEGREE][COLOR_WHITE] = res.getDrawable(R.drawable.ic_sp_degree_white);
			mDrawables[TYPE_FAVORITES][COLOR_BLACK] = res.getDrawable(R.drawable.ic_sp_favorites_black);
			mDrawables[TYPE_FAVORITES][COLOR_WHITE] = res.getDrawable(R.drawable.ic_sp_favorites_white);
			mDrawables[TYPE_COMMENTS][COLOR_BLACK] = res.getDrawable(R.drawable.ic_sp_comments_black);
			mDrawables[TYPE_COMMENTS][COLOR_WHITE] = res.getDrawable(R.drawable.ic_sp_comments_white);
			mDrawables[TYPE_TOPIC][COLOR_BLACK] = res.getDrawable(R.drawable.ic_sp_topic_black);
			mDrawables[TYPE_TOPIC][COLOR_WHITE] = res.getDrawable(R.drawable.ic_sp_topic_white);
		}
		
		public Drawable getTime(boolean isBlack) {
			return mDrawables[TYPE_TIME][isBlack ? COLOR_BLACK : COLOR_WHITE];
		}
		
		public Drawable getDegree(boolean isBlack) {
			return mDrawables[TYPE_DEGREE][isBlack ? COLOR_BLACK : COLOR_WHITE];
		}
		
		public Drawable getFavorites(boolean isBlack) {
			return mDrawables[TYPE_FAVORITES][isBlack ? COLOR_BLACK : COLOR_WHITE];
		}
		
		public Drawable getComments(boolean isBlack) {
			return mDrawables[TYPE_COMMENTS][isBlack ? COLOR_BLACK : COLOR_WHITE];
		}
		
		public Drawable getTopic(boolean isBlack) {
			return mDrawables[TYPE_TOPIC][isBlack ? COLOR_BLACK : COLOR_WHITE];
		}

	}
	
	public static class UI {
		
		/** This class may not be instantiated */
		private UI() { }

		private static final String FADE_IN_PROPERTY = "alpha";
		private static final float[] FADE_IN_VALUES = new float[] { 0.0f, 1.0f };
		private static final int FADE_IN_DURATION = 800;
		
		public static void fadeIn(View view) {
			ObjectAnimator.ofFloat(view, FADE_IN_PROPERTY, FADE_IN_VALUES).setDuration(FADE_IN_DURATION).start();
		}

	}
	
	public static AlertDialog showLoginThrottledInfo(final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.login_throttled_title);
		builder.setMessage(R.string.login_throttled_body);
		builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (activity != null) {
					activity.finish();
				}
			}
		});
		return builder.show();
	}
	
	public static Set<String> getDefaultTopics(Context context) {
		final String[] defaultArray = context.getResources().getStringArray(R.array.topics_list_default);
		final Set<String> out = new HashSet<String>();
		
		for (String entry : defaultArray) {
			if (entry != null) {
				out.add(entry);
			}
		}
		return out;
	}

}