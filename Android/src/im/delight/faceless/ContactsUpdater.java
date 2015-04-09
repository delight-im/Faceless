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

import im.delight.faceless.exceptions.SetupNotCompletedException;
import im.delight.android.tasks.RegularIntentService;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

public class ContactsUpdater extends RegularIntentService {

	private static final int MAX_CONTACTS = 1800;

	@Override
	protected String getLastExecutionPreference() {
		return Global.Preferences.LAST_TIME_CONTACTS_UPDATER;
	}

	@Override
	protected long getInterval() {
		// 12 hours
		return 43200000;
	}

	@Override
	protected int getServiceID() {
		return 1;
	}

	@Override
	protected int getHourMin() {
		return 0;
	}

	@Override
	protected int getHourMax() {
		return 24;
	}

	@Override
	protected int getRandomInterval() {
		// always run as early as possible
		return 0;
	}

	@Override
	protected void run(Intent intent) {
		Global.Setup.load(PreferenceManager.getDefaultSharedPreferences(this));
		final String defaultRegion = Global.Setup.getRegionCode();

		// we can only do something if the setup has been completed
		if (Global.Setup.isComplete()) {
			// set up variables and resources
			StringBuilder contactsCSV = new StringBuilder();
			String[] phoneValidated;

			// load all phone numbers
			Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
			int counter = 0;
			while (phones.moveToNext()) {
				phoneValidated = Global.PhoneWithLib.normalizeNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), defaultRegion);
				if (phoneValidated != null && phoneValidated[0] != null && phoneValidated[0].length() > 0) {
					// when an upper limit for the number of contacts is reached
					if (counter >= MAX_CONTACTS) {
						// cancel the list building process here
						break;
					}
					// for all but the first element
					else if (counter > 0) {
						// add a comma as the separator
						contactsCSV.append(",");
					}
					contactsCSV.append(Global.Crypto.hash(phoneValidated[0]));
					counter++;
				}
			}
			phones.close();

			// send the data to the server (and ignore the response)
			try {
				Server.setFriends(this, contactsCSV.toString(), null);
			}
			catch (SetupNotCompletedException e) { }
		}
	}

}
