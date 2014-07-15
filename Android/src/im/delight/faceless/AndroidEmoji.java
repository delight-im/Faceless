package im.delight.faceless;

/**
 * Copyright 2014 www.delight.im <info@delight.im>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Build;

/** Emoji helper for Android that extends the basic Java Emoji features */
public class AndroidEmoji extends Emoji {
	
	/** AndroidEmoji.ttf font has been added in Android 4.1 and replaced with NotoColorEmoji.ttf in Android 4.4 */
	private static final int ANDROID_EMOJI_MIN_APK_LEVEL = 16;
	
	/**
	 * Ensures that Emoji can be displayed correctly on the current Android version
	 * 
	 * @param text the text containing Unicode Emoji
	 * @return the unchanged text if Unicode Emoji are supported or an updated text for improved compatibility
	 */
	public static String makeCompatible(String text) {
		// check if we have 
		if (Build.VERSION.SDK_INT >= ANDROID_EMOJI_MIN_APK_LEVEL) {
			// return the text without any changes
			return text;
		}
		// if there's no Emoji font available
		else {
			// convert all Unicode Emoji back to emoticons in text format
			return Emoji.replaceInText(text, true);
		}
	}

}
