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

import java.util.Set;
import im.delight.faceless.Server.GetMessagesResponse;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SubscriptionNotifications extends AbstractNotificationSender {

	private SharedPreferences mPrefs;

	@Override
	protected String getLastExecutionPreference() {
		return Config.Preferences.LAST_TIME_SUBSCRIPTION_NOTIFICATIONS;
	}

	@Override
	protected long getInterval() {
		// depending on the notification frequency defined in the settings
		createPreferencesIfNecessary();
		final String frequency = mPrefs.getString(Config.Preferences.SUBSCRIPTION_NOTIFICATIONS_FREQUENCY, "frequently");
		if (frequency.equals("frequently")) {
			// every 30 minutes
			return 1800000;
		}
		else if (frequency.equals("moderately")) {
			// every 3 hours
			return 10800000;
		}
		else if (frequency.equals("off")) {
			// every year
			return 31536000000L;
		}
		// rarely
		else {
			// every 12 hours
			return 43200000;
		}
	}

	@Override
	protected int getServiceID() {
		return 3;
	}

	@Override
	protected int getHourMin() {
		return 9;
	}

	@Override
	protected int getHourMax() {
		return 21;
	}

	@Override
	protected int getRandomInterval() {
		// run between 0 and 15 minutes after the earliest possible time
		return 900000;
	}
	
	private void createPreferencesIfNecessary() {
		if (mPrefs == null) {
			mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		}
	}

	@Override
	protected void run(Intent intent) {
		createPreferencesIfNecessary();
		Global.Setup.load(mPrefs);
		
		sendSubscriptionNotification();
	}
	
	private void sendSubscriptionNotification() {
		final Set<String> topicsList = mPrefs.getStringSet(ActivitySettings.PREF_TOPICS_LIST, Global.getDefaultTopics(this));
		final GetMessagesResponse subscriptionUpdates = Server.getMessagesSync(this, Server.MODE_SUBSCRIPTIONS, 0, topicsList);
		final int subscriptionUpdateCount = subscriptionUpdates == null ? 0 : subscriptionUpdates.subscriptionUpdates;
		
		if (subscriptionUpdateCount > 0) {		
			// build the text for the notification
			String notificationText = getResources().getQuantityString(R.plurals.x_subscription_updates, subscriptionUpdateCount, subscriptionUpdateCount);
			
			// set up the notification informing the user about their new friend count
			sendNotification(ActivityMain.class, App.NOTIFICATION_ID_SUBSCRIPTIONS, getString(R.string.app_name), notificationText, R.drawable.ic_notification_small, R.drawable.ic_launcher);
		}
	}

}
