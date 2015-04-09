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
import java.util.Set;
import im.delight.faceless.Server.GetMessagesResponse;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GeneralNotifications extends AbstractNotificationSender {

	private SharedPreferences mPrefs;

	@Override
	protected String getLastExecutionPreference() {
		return Global.Preferences.LAST_TIME_GENERAL_NOTIFICATIONS;
	}

	@Override
	protected long getInterval() {
		// 24 hours
		return 86400000;
	}

	@Override
	protected int getServiceID() {
		return 2;
	}

	@Override
	protected int getHourMin() {
		return 14;
	}

	@Override
	protected int getHourMax() {
		return 20;
	}

	@Override
	protected int getRandomInterval() {
		// run between 0 and 2.5 hours after the earliest possible time
		return 9000000;
	}

	@Override
	protected void run(Intent intent) {
		// get access to the preferences
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Global.Setup.load(mPrefs);

		sendFriendsNotification();
		sendMessagesNotification();
	}

	private void sendFriendsNotification() {
		try {
			final int friendsCount = Server.getFriendsCountSync(this);
			int oldFriendsCount = mPrefs.getInt(Global.Preferences.FRIENDS_COUNT, 0);

			if (friendsCount > oldFriendsCount) {
				// update the friends count in the preferences
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putInt(Global.Preferences.FRIENDS_COUNT, friendsCount);
				editor.apply();

				// build the text for the notification
				String notificationText = getResources().getQuantityString(R.plurals.x_friends_using_app, friendsCount, friendsCount);

				// set up the notification informing the user about their new friend count
				sendNotification(ActivityMain.class, App.NOTIFICATION_ID_FRIENDS, getString(R.string.app_name), notificationText, R.drawable.ic_notification_small, R.drawable.ic_launcher);
			}
		}
		catch (SetupNotCompletedException e) { }
	}

	private void sendMessagesNotification() {
		try {
			final Set<String> topicsList = mPrefs.getStringSet(ActivitySettings.PREF_TOPICS_LIST, Global.getDefaultTopics(this));
			final GetMessagesResponse messages = Server.getMessagesSync(this, Server.MODE_FRIENDS, 0, null, topicsList, mPrefs.getInt(Global.Preferences.FRIENDS_COUNT, 0));

			if (messages != null) {
				// get the ID of the latest message that has already been read
				final long latestMessageRead = mPrefs.getLong(Global.Preferences.LATEST_MESSAGE_READ, 0);

				// prepare a variable holding the new ID of the latest message read
				long newLatestMessageRead = 0;

				// set up the counter for the number of unread messages
				int unreadMessagesFromFriends = 0;

				// set up a temporary variable that will hold the message ID
				long messageID;

				// iterate over all messages
				for (Message message : messages.messages) {
					if (message != null) {
						// if the message is from a friend or friend of a friend
						if (message.getDegree() == 1 || message.getDegree() == 2) {
							try {
								messageID = message.getIDNumber();

								if (messageID > latestMessageRead) {
									// increase the counter of unread messages by one
									unreadMessagesFromFriends++;

									// remember the new ID of the latest message read
									if (messageID > newLatestMessageRead) {
										newLatestMessageRead = messageID;
									}
								}
							}
							catch (Exception e) { }
						}
					}
				}

				// update the ID of the latest message read in the preferences
				if (newLatestMessageRead > latestMessageRead) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putLong(Global.Preferences.LATEST_MESSAGE_READ, newLatestMessageRead);
					editor.apply();
				}

				// if we have some unread messages from friends
				if (unreadMessagesFromFriends > 0) {
					// build the text for the notification
					String notificationText = getResources().getQuantityString(R.plurals.x_unread_messages, unreadMessagesFromFriends, unreadMessagesFromFriends);

					// set up the notification informing the user about their new friend count
					sendNotification(ActivityMain.class, App.NOTIFICATION_ID_MESSAGES, getString(R.string.app_name), notificationText, R.drawable.ic_notification_small, R.drawable.ic_launcher);
				}
			}
		}
		catch (SetupNotCompletedException e) { }
	}

}
