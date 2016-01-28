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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class ActivitySettings extends PreferenceActivity {
	
	public static final String PREF_LANGUAGE = "language";
	public static final String PREF_COUNTRY = "country";
	public static final String PREF_TOPICS_LIST = "topicsList";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, ActivityMain.class)); // go one step up in Activity hierarchy
		finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		CustomLanguage.setLanguage(this, prefs.getString(PREF_LANGUAGE, ""), true);
	}

}
