package im.delight.faceless;

/*
 * Copyright (c) delight.im <info@delight.im>
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

import im.delight.android.languages.CustomLanguage;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class App extends Application {
	
	public static final int NOTIFICATION_ID_FRIENDS = 1;
	public static final int NOTIFICATION_ID_MESSAGES = 2;
	public static final int NOTIFICATION_ID_SUBSCRIPTIONS = 3;
	
	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		CustomLanguage.setLanguage(this, prefs.getString(ActivitySettings.PREF_LANGUAGE, ""));
	}

}
