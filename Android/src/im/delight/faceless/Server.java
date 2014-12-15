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

import im.delight.android.location.SimpleLocation;
import im.delight.android.baselib.Collections;
import im.delight.android.baselib.Data;
import im.delight.android.languages.CustomLanguage;
import im.delight.android.webrequest.WebRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class Server {

	public static final int MODE_FRIENDS = 1;
	public static final int MODE_POPULAR = 2;
	public static final int MODE_LATEST = 3;
	public static final int MODE_FAVORITES = 4;
	public static final int MODE_SUBSCRIPTIONS = 5;
	public static final int MODE_NEARBY = 6;
	public static final int STATUS_OK = 1;
	public static final int STATUS_MAINTENANCE = 2;
	public static final int STATUS_BAD_REQUEST = 3;
	public static final int STATUS_OUTDATED_CLIENT = 4;
	public static final int STATUS_NOT_AUTHORIZED = 5;
	public static final int STATUS_TEMPORARILY_BANNED = 6;
	public static final int STATUS_LOGIN_THROTTLED = 7;
	public static final int STATUS_NO_CONNECTION = 8;
	protected static final int MESSAGES_PER_REQUEST = 50;

	public static class Callback {

		// instances of this class are not allowed
		private Callback() { }

		public interface MessageEvent {
			public void onReceivedMessages(int status, int mode, int page, boolean reachedEnd, final long latestMessageID, final int subscriptionUpdates, List<Message> messages);
			public void onReceivedDetails(int status, boolean isFavorited, boolean isSubscribed);
			public void onSentMessage(int status, String messageText, String messageTopic, String messageID, long messageTime, String messageColorHex, int messagePatternID, String messageCountryISO3);
		}

		public interface CommentEvent {
			public void onReceivedComments(int status, List<Comment> comments, boolean hasPrivateComments);
			public void onSentComment(int status, String commentText, String commentID, int ownerInThread, int privateRecipientInThread, long commentTime);
		}

		public interface FavoriteEvent {
			public void onChangedFavorite(int status, boolean favorited);
		}

		public interface SubscriptionEvent {
			public void onChangedSubscription(int status, boolean subscribed);
			public void onClearedSubscriptions(int status);
		}

		public interface ConnectionEvent {
			public void onAddedFriend(int status);
			public void onAddedBlock(int status);
		}

		public interface ReportEvent {
			public void onSentReport(int status);
		}

		public interface VerificationEvent {
			public void onLoadVerification();
			public void onPreparedVerification(int status, String apiPhoneNumber, String verificationCode, long validUntil);
		}

	}

	public static class GetMessagesResponse {
		public boolean reachedEnd;
		public long latestMessageID;
		public List<Message> messages;
		public int subscriptionUpdates;
	}

	public static GetMessagesResponse getMessagesSync(final Context context, final int mode, final int page, final SimpleLocation.Point location, final Set<String> topicsList, final int friendsCount) {
		WebRequest request = getMessagesRequest(context, mode, page, location, topicsList);
		final String responseText = request.executeSync();
		final int status = parseStatus(responseText);

		if (status == STATUS_OK) {
			GetMessagesResponse response = parseMessages(context, mode, page, responseText, friendsCount);
			return response;
		}
		else {
			return null;
		}
	}

	public static void getMessagesAsync(final Context context, final int mode, final int page, final SimpleLocation.Point location, final Set<String> topicsList, final int friendsCount, final Callback.MessageEvent callback) {
		WebRequest request = getMessagesRequest(context, mode, page, location, topicsList);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				final int status = parseStatus(responseText);
				if (status == STATUS_OK) {
					GetMessagesResponse response = parseMessages(context, mode, page, responseText, friendsCount);
					if (callback != null) {
						callback.onReceivedMessages(status, mode, page, response.reachedEnd, response.latestMessageID, response.subscriptionUpdates, response.messages);
					}
				}
				else {
					if (callback != null) {
						callback.onReceivedMessages(status, mode, page, true, 0, -1, null);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onReceivedMessages(STATUS_NO_CONNECTION, mode, page, true, 0, -1, null);
				}
			}
		});
	}

	public static void getMessageDetails(final Context context, final String messageID, final Callback.MessageEvent callback) {
		WebRequest request = new APIRequest(context).get().to("/messages/details");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("messageID", messageID);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				final int status = parseStatus(responseText);
				if (status == STATUS_OK) {
					try {
						final JSONObject responseData = new JSONObject(responseText);
						final boolean isFavorited = responseData.getBoolean("isFavorited");
						final boolean isSubscribed = responseData.getBoolean("isSubscribed");
						if (callback != null) {
							callback.onReceivedDetails(status, isFavorited, isSubscribed);
						}
					}
					catch (Exception e) {
						if (callback != null) {
							callback.onReceivedDetails(status, false, false);
						}
					}
				}
				else {
					if (callback != null) {
						callback.onReceivedDetails(status, false, false);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onReceivedDetails(STATUS_NO_CONNECTION, false, false);
				}
			}
		});
	}

	public static void saveMessage(final Context context, final String colorHex, final int patternID, final String text, final String topic, final String visibility, final SimpleLocation.Point location, final Callback.MessageEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/messages/new");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("colorHex", colorHex);
		request.addParam("patternID", patternID);
		request.addParam("text", text);
		request.addParam("topic", topic);
		request.addParam("visibility", visibility);
		try {
			request.addParam("languageISO3", Locale.getDefault().getISO3Language().toUpperCase(Locale.US));
		}
		catch (Exception e) { }
		String countryIso3 = null;
		try {
			// get the user's country from the preferences
			countryIso3 = PreferenceManager.getDefaultSharedPreferences(context).getString(ActivitySettings.PREF_COUNTRY, null);
			// if the country is not set in the preferences yet
			if (countryIso3 == null || countryIso3.length() != 3) {
				// use the Locale's country property as the default
				countryIso3 = Locale.getDefault().getISO3Country().toUpperCase(Locale.US);
				if (countryIso3 == null || countryIso3.length() != 3) {
					Locale originalLocale = CustomLanguage.getOriginalLocale();
					if (originalLocale != null) {
						countryIso3 = originalLocale.getISO3Country().toUpperCase(Locale.US);
					}
				}
			}
			request.addParam("countryISO3", countryIso3);
		}
		catch (Exception e) { }

		if (location != null) {
			request.addParam("location[lat]", location.latitude);
			request.addParam("location[long]", location.longitude);
		}

		final String messageCountryIso3 = countryIso3;
		request.addParam("random", UUID.randomUUID().toString());
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				try {
					final JSONObject responseData = new JSONObject(responseText);
					final String messageID = responseData.getString("messageID");
					final long messageTime = responseData.getInt("messageTime") * 1000L;
					if (callback != null) {
						callback.onSentMessage(parseStatus(responseText), text, topic, messageID, messageTime, colorHex, patternID, messageCountryIso3);
					}
				}
				catch (Exception e) {
					if (callback != null) {
						callback.onSentMessage(parseStatus(responseText, true), null, null, null, 0, null, 0, null);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onSentMessage(STATUS_NO_CONNECTION, null, null, null, 0, null, 0, null);
				}
			}
		});
	}

	public static void getComments(final Context context, final String messageID, final Callback.CommentEvent callback) {
		WebRequest request = new APIRequest(context).get().to("/comments/list");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("messageID", messageID);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				final int status = parseStatus(responseText);
				if (status == STATUS_OK) {
					List<Comment> comments = new LinkedList<Comment>();
					boolean hasPrivateComments = false;
					try {
						final JSONObject responseData = new JSONObject(responseText);
						final JSONArray responseComments = responseData.getJSONArray("comments");
						final int responseCommentCount = responseComments.length();
						int privateRecipientInThread;
						for (int i = 0; i < responseCommentCount; i++) {
							JSONObject responseMessage = (JSONObject) responseComments.get(i);
							privateRecipientInThread = responseMessage.isNull("privateRecipientInThread") ? 0 : responseMessage.getInt("privateRecipientInThread");
							if (privateRecipientInThread > 0) {
								hasPrivateComments = true;
							}
							comments.add(new Comment(responseMessage.getString("id"), responseMessage.getString("text"), privateRecipientInThread, responseMessage.getBoolean("isOwner"), responseMessage.getBoolean("isSelf"), (responseMessage.isNull("ownerInThread") ? 0 : responseMessage.getInt("ownerInThread")), responseMessage.getInt("time") * 1000L));
						}
					}
					catch (Exception e) { }
					if (callback != null) {
						callback.onReceivedComments(status, comments, hasPrivateComments);
					}
				}
				else {
					if (callback != null) {
						callback.onReceivedComments(status, null, false);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onReceivedComments(STATUS_NO_CONNECTION, null, false);
				}
			}
		});
	}

	public static void addComment(final Context context, final String messageID, final String privateReplyToCommentID, final String text, final Callback.CommentEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/comments/new");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("messageID", messageID);
		if (privateReplyToCommentID != null) {
			request.addParam("privateReplyToComment", privateReplyToCommentID);
		}
		request.addParam("text", text);
		request.addParam("random", UUID.randomUUID().toString());
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				try {
					final JSONObject responseData = new JSONObject(responseText);
					if (callback != null) {
						callback.onSentComment(parseStatus(responseText), text, responseData.getString("commentID"), (responseData.isNull("ownerInThread") ? 0 : responseData.getInt("ownerInThread")), (responseData.isNull("privateRecipientInThread") ? 0 : responseData.getInt("privateRecipientInThread")), (responseData.getInt("commentTime") * 1000L));
					}
				}
				catch (Exception e) {
					if (callback != null) {
						callback.onSentComment(parseStatus(responseText, true), null, null, 0, 0, 0);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onSentComment(STATUS_NO_CONNECTION, null, null, 0, 0, 0);
				}
			}
		});
	}

	public static void setFavorited(final Context context, final String messageID, final boolean favorited, final Callback.FavoriteEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/favorites/set");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("messageID", messageID);
		request.addParam("favorited", favorited ? 1 : 0);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onChangedFavorite(parseStatus(responseText), favorited);
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onChangedFavorite(STATUS_NO_CONNECTION, false);
				}
			}
		});
	}

	public static void setSubscribed(final Context context, final String messageID, final boolean subscribed, final Callback.SubscriptionEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/subscriptions/set");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("messageID", messageID);
		request.addParam("subscribed", subscribed ? 1 : 0);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onChangedSubscription(parseStatus(responseText), subscribed);
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onChangedSubscription(STATUS_NO_CONNECTION, false);
				}
			}
		});
	}

	public static void clearSubscriptions(final Context context, final Callback.SubscriptionEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/subscriptions/clear");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onClearedSubscriptions(parseStatus(responseText));
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onClearedSubscriptions(STATUS_NO_CONNECTION);
				}
			}
		});
	}

	public static void setFriends(final Context context, final String usernamesCSV, final Callback.ConnectionEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/connections/friend");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("userList", usernamesCSV);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onAddedFriend(parseStatus(responseText));
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onAddedFriend(STATUS_NO_CONNECTION);
				}
			}
		});
	}

	public static int getFriendsCountSync(final Context context) {
		WebRequest request = new APIRequest(context).get().to("/connections/friend/count");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		final String responseText = request.executeSync();

		final int status = parseStatus(responseText);
		if (status == STATUS_OK) {
			try {
				final JSONObject responseData = new JSONObject(responseText);
				return responseData.getInt("friends");
			}
			catch (Exception e) {
				return -1;
			}
		}
		else {
			return -1;
		}
	}

	public static void setBlocked(final Context context, final String contentType, final String contentID, final Callback.ConnectionEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/connections/block");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("contentType", contentType);
		request.addParam("contentID", contentID);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onAddedBlock(parseStatus(responseText));
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onAddedBlock(STATUS_NO_CONNECTION);
				}
			}
		});
	}

	public static void sendReport(final Context context, final String contentType, final String contentID, final int reason, final Callback.ReportEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/reports/new");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.addParam("contentType", contentType);
		request.addParam("contentID", contentID);
		request.addParam("reason", reason);
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				if (callback != null) {
					callback.onSentReport(parseStatus(responseText));
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onSentReport(STATUS_NO_CONNECTION);
				}
			}
		});
	}

	public static void prepareVerification(final Context context, final Callback.VerificationEvent callback) {
		WebRequest request = new APIRequest(context).post().to("/verifications/prepare");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		request.executeAsync(new WebRequest.Callback() {
			@Override
			public void onSuccess(String responseText) {
				final int status = parseStatus(responseText);
				if (status == STATUS_OK) {
					String apiPhoneNumber = null;
					String verificationCode = null;
					long validUntil = 0;
					try {
						final JSONObject responseData = new JSONObject(responseText);
						apiPhoneNumber = responseData.getString("apiPhoneNumber");
						verificationCode = responseData.getString("verificationCode");
						validUntil = responseData.getLong("validUntil");
					}
					catch (Exception e) { }
					if (callback != null) {
						callback.onPreparedVerification(status, apiPhoneNumber, verificationCode, validUntil);
					}
				}
				else {
					if (callback != null) {
						callback.onPreparedVerification(status, null, null, 0);
					}
				}
			}
			@Override
			public void onError() {
				if (callback != null) {
					callback.onPreparedVerification(STATUS_NO_CONNECTION, null, null, 0);
				}
			}
		});
	}

	protected static WebRequest getMessagesRequest(final Context context, final int mode, final int page, final SimpleLocation.Point location, final Set<String> topicsList) {
		// add meta category to list of topics as this should be visible to all users
		topicsList.add("meta");

		WebRequest request = new APIRequest(context).get().to("/messages/list");
		request.auth(Global.Setup.getUsername(), Global.Setup.getPassword());
		if (mode == MODE_FRIENDS) {
			request.addParam("mode", "friends");
		}
		else if (mode == MODE_POPULAR) {
			request.addParam("mode", "popular");
		}
		else if (mode == MODE_LATEST) {
			request.addParam("mode", "latest");
		}
		else if (mode == MODE_FAVORITES) {
			request.addParam("mode", "favorites");
		}
		else if (mode == MODE_SUBSCRIPTIONS) {
			request.addParam("mode", "subscriptions");
		}
		else if (mode == MODE_NEARBY) {
			if (location != null) {
				request.addParam("location[lat]", location.latitude);
				request.addParam("location[long]", location.longitude);
			}
			else {
				throw new RuntimeException("Location required for nearby mode");
			}
			request.addParam("mode", "nearby");
		}
		else {
			throw new RuntimeException("Unknown mode: "+mode);
		}
		request.addParam("page", page);
		request.addParam("topicsList", Collections.implode(topicsList, ","));
		try {
			request.addParam("languageISO3", Locale.getDefault().getISO3Language().toUpperCase(Locale.US));
		}
		catch (Exception e) { }
		return request;
	}

	protected static GetMessagesResponse parseMessages(final Context context, final int mode, final int page, final String responseText, final int friendsCount) {
		List<Message> messages = new LinkedList<Message>();
		long oldestMessageTime = System.currentTimeMillis();
		long latestMessageID = 0;
		int subscriptionUpdates = -1;
		if (!Config.DEMO_ACTIVE) {
			try {
				final JSONObject responseData = new JSONObject(responseText);
				final JSONArray responseMessages = responseData.getJSONArray("messages");
				final int responseMessageCount = responseMessages.length();

				long responseMessageTime;
				int messageDegree;
				int messageType;
				SimpleLocation.Point messageLocation;
				for (int i = 0; i < responseMessageCount; i++) {
					JSONObject responseMessage = (JSONObject) responseMessages.get(i);
					responseMessageTime = responseMessage.getInt("time") * 1000L;
					if (responseMessageTime < oldestMessageTime) {
						oldestMessageTime = responseMessageTime;
					}
					try {
						latestMessageID = Math.max(latestMessageID, Message.getIDNumber(responseMessage.getString("id")));
					}
					catch (Exception e) { }
					messageDegree = responseMessage.getInt("degree");
					messageType = Message.Type.fromProperties(messageDegree, friendsCount);
					messageLocation = parseLocation(responseMessage, "location");
					messages.add(new Message(responseMessage.getString("id"), messageDegree, responseMessage.getString("colorHex"), responseMessage.getInt("patternID"), responseMessage.getString("text"), responseMessage.getString("topic"), responseMessageTime, responseMessage.getInt("favoritesCount"), responseMessage.getInt("commentsCount"), responseMessage.getString("countryISO3"), messageType, messageLocation));
				}

				subscriptionUpdates = responseData.getInt("subscriptionUpdates");
			}
			catch (Exception e) {
				GetMessagesResponse out = new GetMessagesResponse();
				out.reachedEnd = true;
				out.latestMessageID = latestMessageID;
				out.messages = messages;
				out.subscriptionUpdates = subscriptionUpdates;
				return out;
			}
		}
		else {
			// pretend to have some subscription updates for demo purposes
			subscriptionUpdates = 7;
		}
		// count messages that have been returned from the server
		final int messageCount = messages.size();
		// if users have not enough messages in their feed yet
		if (messageCount < MESSAGES_PER_REQUEST && page == 0 && mode != MODE_SUBSCRIPTIONS && mode != MODE_FAVORITES) {
			// get some example messages
			List<Message> exampleMessages = getExampleMessages(context, (MESSAGES_PER_REQUEST - messageCount), oldestMessageTime);
			// add the examples to the response
			messages.addAll(exampleMessages);
		}

		GetMessagesResponse out = new GetMessagesResponse();
		out.reachedEnd = messageCount < MESSAGES_PER_REQUEST;
		out.latestMessageID = latestMessageID;
		out.messages = messages;
		out.subscriptionUpdates = subscriptionUpdates;

		return out;
	}

	protected static SimpleLocation.Point parseLocation(final JSONObject jsonObj, final String locationFieldKey) {
		try {
			if (!jsonObj.isNull(locationFieldKey)) {
				JSONObject locationObj = jsonObj.getJSONObject(locationFieldKey);
				if (locationObj != null) {
					if (!locationObj.isNull("lat") && !locationObj.isNull("long")) {
						return new SimpleLocation.Point(locationObj.getDouble("lat"), locationObj.getDouble("long"));
					}
				}
			}
		}
		catch (Exception e) { }

		return null;
	}

	protected static List<Message> getExampleMessages(Context context, int number, long startTime) {
		final List<Message> out = new LinkedList<Message>();
		final String[] exampleMessages = context.getResources().getStringArray(R.array.example_messages);
		number = Math.min(number, exampleMessages.length);

		int color;
		String colorHex;
		long messageTime;
		int comments;
		int messageType;
		for (int i = 0; i < number; i++) {
			// create arbitrary but constant (on subsequent requests) colors
			color = getExampleColor(i);
			// turn color into hex value
			colorHex = Data.colorToHex(color);
			// create arbitrary but constant (on subsequent requests) message timestamps
			messageTime = startTime - ((i+1) * 3600L * 12L * 1000L);

			if (Config.DEMO_ACTIVE) {
				messageType = Message.Type.NORMAL;
			}
			else {
				messageType = Message.Type.EXAMPLE;
			}

			comments = 12 - (i * 3);
			if (comments < 0) {
				comments = 0;
			}
			out.add(new Message(null, ((i+1) % 4), colorHex, i, exampleMessages[i], "life", messageTime, 4, comments, null, messageType, null));
		}

		return out;
	}

	protected static int getExampleColor(int n) {
		// don't start with a color that is too dark (value produces nice colors and has been retrieved experimentally)
		n = n+9;
		// use RGB values between 42 and 214 to prevent black and white
		// use different slopes for R/G/B to prevent grayscale colors
		return Color.rgb((42 + ((n * 45) % 172)), (42 + ((n * 75) % 172)), (42 + ((n * 105) % 172)));
	}

	protected static int parseStatus(final String responseText) {
		return parseStatus(responseText, false);
	}

	protected static int parseStatus(final String responseText, final boolean requireError) {
		try {
			JSONObject json = new JSONObject(responseText);
			final String status = json.getString("status");
			if (status.equals("ok")) {
				if (requireError) {
					return STATUS_BAD_REQUEST;
				}
				else {
					return STATUS_OK;
				}
			}
			else if (status.equals("maintenance")) {
				return STATUS_MAINTENANCE;
			}
			else if (status.equals("bad_request")) {
				return STATUS_BAD_REQUEST;
			}
			else if (status.equals("outdated_client")) {
				return STATUS_OUTDATED_CLIENT;
			}
			else if (status.equals("not_authorized")) {
				return STATUS_NOT_AUTHORIZED;
			}
			else if (status.equals("temporarily_banned")) {
				return STATUS_TEMPORARILY_BANNED;
			}
			else if (status.equals("login_throttled")) {
				return STATUS_LOGIN_THROTTLED;
			}
		}
		catch (Exception e) { }
		return STATUS_NO_CONNECTION;
	}

}
