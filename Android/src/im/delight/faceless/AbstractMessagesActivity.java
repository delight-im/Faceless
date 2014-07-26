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

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import im.delight.android.baselib.Social;
import im.delight.android.baselib.UI;
import im.delight.android.infinitescrolling.InfiniteScrollListener;
import im.delight.android.progress.SimpleProgressDialog;
import im.delight.apprater.AppRater;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public abstract class AbstractMessagesActivity extends Activity implements OnRefreshListener, Server.Callback.MessageEvent, Server.Callback.VerificationEvent {

	/** The duration in milliseconds after which a refresh of the message list should be enforced */
	private static final int ACTIVITY_REFRESH_INTERVAL = 600000;
	private SharedPreferences mPrefs;
	private LayoutInflater mInflater;
	protected Global.MessagePropertyDrawables mMessagePropertyDrawables;
	protected Resources mResources;
	private BackgroundPatterns mBackgroundPatterns;
	private SimpleProgressDialog mSimpleProgressDialog;
	protected int mSubscriptionUpdates;
	private long mLastRefresh;
	protected AlertDialog mAlertDialog;
	// LISTVIEW WITH ADAPTER AND PROGRESSBAR BEGIN
	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView mListView;
	private ProgressBar mProgressBarLoading;
	protected MessagesAdapter mAdapter;
	private AdapterView.OnItemClickListener mMessageClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, final View v, int position, long id) {
			final Message message;
			try {
				message = mAdapter.getItem(position);
			}
			catch (Exception e) {
				return;
			}
			if (message == null) {
				return;
			}
			if (message.isAdminMessage()) {
				startActivity(new Intent(AbstractMessagesActivity.this, ActivityInvite.class));
			}
			else if (message.isExampleMessage()) {
				Toast.makeText(AbstractMessagesActivity.this, R.string.example_message_explanation, Toast.LENGTH_SHORT).show();
			}
			else {
				Intent intentDetails = new Intent(AbstractMessagesActivity.this, ActivityDetails.class);
				intentDetails.putExtra(ActivityDetails.EXTRA_MESSAGE, message);
				startActivity(intentDetails);
			}
		}
	};
	private InfiniteScrollListener mInfiniteScrollListener = new InfiniteScrollListener(5, 10) {

		@Override
		public void onReloadItems(int pageToRequest) {
			reloadMessages(getMessagesMode(), pageToRequest, false, false, true);
		}

		@Override
		public void onReloadFinished() { }
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int itemsTotal) {
			super.onScroll(view, firstVisibleItem, visibleItemCount, itemsTotal);
			isScrollTop(firstVisibleItem <= 0);
		}

	};
	// LISTVIEW WITH ADAPTER AND PROGRESSBAR END
	
	abstract protected int getLayoutResourceID();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceID());
		
		// set up access to the preferences
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Global.Setup.load(mPrefs);
		
		// check if introduction has already been completed
		if (mPrefs.getInt(Config.Preferences.INTRO_STEP, 0) < Integer.MAX_VALUE) {
			// if introduction has not yet been completed
			// switch to introduction screen
			startActivity(new Intent(this, ActivityIntro.class));
			// prevent any window animation because the user is to be redirected immediately
			overridePendingTransition(0, 0);
			finish();
			return;
		}

		// check if setup has been completed
		if (!Global.Setup.isComplete()) {
			// try to run automatic setup
			if (!Global.Setup.runAuto(this, mPrefs)) { 
				// if automatic setup did not succeed
				// switch to manual setup
				startActivity(new Intent(this, ActivitySetup.class));
				// prevent any window animation because the user is to be redirected immediately
				overridePendingTransition(0, 0);
				finish();
				return;
			}
		}
		
		// set up the layout inflater
		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		// set up other resources
		mResources = getResources();
		mMessagePropertyDrawables = new Global.MessagePropertyDrawables(this);
		mBackgroundPatterns = BackgroundPatterns.getInstance(this);

		// set up the two mode tabs
		setupButtonBar();
		
		// set up the ListView with its ArrayAdapter and the ProgressBar
		mListView = (ListView) findViewById(R.id.listViewMessages);
		mProgressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
		mAdapter = new MessagesAdapter(this, R.layout.row_messages_list, new ArrayList<Message>());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mMessageClickListener);
		
		// set up pull-to-refresh (at the top)
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.viewListViewContainer);
		ActionBarPullToRefresh.from(this).allChildrenArePullable().listener(this).setup(mPullToRefreshLayout);
		
		// set up infinite scrolling (at the bottom)
		mListView.setOnScrollListener(mInfiniteScrollListener);
		
		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(isActionBarUpEnabled());
		
		// load first data into the ListView
		reloadMessages(getMessagesMode(), 0, true, false, false);
		
		// prompt the user to rate the app if this is appropriate
		AppRater appRater = new AppRater(this);
		appRater.setPhrases(R.string.app_rater_title, R.string.app_rater_explanation, R.string.app_rater_now, R.string.app_rater_later, R.string.app_rater_never);
		appRater.show();
	}
	
	abstract protected boolean isActionBarUpEnabled();
	
	abstract protected int getMessagesMode();
	
	abstract protected void setupButtonBar();
	
	protected void setLoading(boolean loading) {
		if (loading) {
			mSimpleProgressDialog = SimpleProgressDialog.show(this);
		}
		else {
			if (mSimpleProgressDialog != null) {
				mSimpleProgressDialog.dismiss();
				mSimpleProgressDialog = null;
			}
		}
	}
	
	protected void reloadMessages(int mode, int page, boolean isInitial, boolean isPullToRefresh, boolean isInfiniteScrolling) {
		if (isInitial) {
			mPullToRefreshLayout.setVisibility(View.GONE);
			mProgressBarLoading.setVisibility(View.VISIBLE);
		}
		else if (isPullToRefresh) {
			mPullToRefreshLayout.setRefreshing(true);
		}
		else if (isInfiniteScrolling) {
			setLoading(true);
		}

		final Set<String> topicsList = mPrefs.getStringSet(ActivitySettings.PREF_TOPICS_LIST, Global.getDefaultTopics(this));
		Server.getMessagesAsync(AbstractMessagesActivity.this, mode, page, topicsList, AbstractMessagesActivity.this);
	}
	
	private static String getVerificationMessageText(String verificationCode) {
		StringBuilder out = new StringBuilder();
		out.append("Verification text -- ");
		out.append(verificationCode);
		out.append(" -- Please do not change!");
		return out.toString();
	}

	public static class MessagesAdapterViews {
		public View viewMessageContainer;
		public TextView textViewMessage;
		public TextView textViewDegree;
		public TextView textViewComments;
		private int textColor;
	}

	public class MessagesAdapter extends ArrayAdapter<Message> {
			
		public MessagesAdapter(Context context, int textViewResourceId, List<Message> items) {
			super(context, textViewResourceId, items);
		}
			
		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setItems(Collection<? extends Message> items) {
			clear();
			addAll(items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MessagesAdapterViews holder;
			View v = convertView;
			final Message o = getItem(position);
			if (v == null) { // view must be initialized (it is a new one)
				v = mInflater.inflate(R.layout.row_messages_list, parent, false);
				holder = new MessagesAdapterViews();
				holder.viewMessageContainer = v.findViewById(R.id.viewMessageContainer);
				holder.textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);
				holder.textViewMessage.setTypeface(FontProvider.getInstance(AbstractMessagesActivity.this).getFontRegular());
				holder.textViewDegree = (TextView) v.findViewById(R.id.textViewDegree);
				holder.textViewComments = (TextView) v.findViewById(R.id.textViewComments);
				v.setTag(holder);
			}
			else { // view is already initialized (old view could be recycled)
				holder = (MessagesAdapterViews) v.getTag();
			}
			if (o != null) {
				holder.textColor = UI.getTextColor(o.getColor());

				// update contents
				mBackgroundPatterns.setViewBackground(AbstractMessagesActivity.this, holder.viewMessageContainer, o.getPatternID(), o.getColor());
				holder.textViewMessage.setText(AndroidEmoji.ensure(o.getText(), AbstractMessagesActivity.this));
				holder.textViewDegree.setText(o.getDegreeText(AbstractMessagesActivity.this));
				if (o.isAdminMessage()) {
					holder.textViewComments.setText(R.string.learn_more);
				}
				else {
					holder.textViewComments.setText(mResources.getQuantityString(R.plurals.x_comments, o.getComments(), o.getComments()));
				}

				// update text and shadow colors
				holder.textViewMessage.setTextColor(holder.textColor);
				holder.textViewMessage.setLinkTextColor(holder.textColor);
				holder.textViewDegree.setTextColor(holder.textColor);
				holder.textViewComments.setTextColor(holder.textColor);
				
				// update property images (TextViews' compound drawables)
				holder.textViewDegree.setCompoundDrawablesWithIntrinsicBounds(mMessagePropertyDrawables.getDegree(holder.textColor == Color.BLACK), null, null, null);
				holder.textViewComments.setCompoundDrawablesWithIntrinsicBounds(null, null, mMessagePropertyDrawables.getComments(holder.textColor == Color.BLACK), null);
				
				// slowly fade in the text
				Global.UI.fadeIn(holder.textViewMessage);
			}
			return v;
		}

	}

	@Override
	public void onRefreshStarted(View view) {
		reloadMessages(getMessagesMode(), 0, false, true, false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// if the Activity has not been refreshed for a longer time
		if (mLastRefresh < (System.currentTimeMillis() - ACTIVITY_REFRESH_INTERVAL)) {
			// force a refresh of the message list
			reloadMessages(getMessagesMode(), 0, true, false, false);
		}
		startService(new Intent(this, GeneralNotifications.class));
		startService(new Intent(this, SubscriptionNotifications.class));
	}
	
	protected void onDestroy() {
		super.onDestroy();
		setLoading(false);
    	startService(new Intent(this, ContactsUpdater.class));
	}
	
	abstract protected void onUpdated(int status, int page);
	
	abstract protected void isScrollTop(boolean isAtTop);

	@Override
	public void onReceivedMessages(final int status, final int mode, final int page, final boolean reachedEnd, long latestMessageID, final int subscriptionUpdates, final List<Message> messages) {
		// get the ID of the latest message read
		long latestMessageRead = mPrefs.getLong(Config.Preferences.LATEST_MESSAGE_READ, 0);
		
		// update the ID of the latest message read if necessary
		if (latestMessageID > latestMessageRead) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putLong(Config.Preferences.LATEST_MESSAGE_READ, latestMessageID);
			editor.apply();
		}
		
		// randomly insert promotion for "Invite friends"
		if (status == Server.STATUS_OK && mode != Server.MODE_SUBSCRIPTIONS && messages != null) {
			final int messagesCount = messages.size();
			if (messagesCount >= 10) {
				final Random random = new Random();
				final int randomPromoPosition = random.nextInt(messagesCount+1);
				final Message promoMessage = new Message(null, Message.DEGREE_ADMIN, "#000000", 0, getString(R.string.action_invite_promo), "meta", System.currentTimeMillis()-300, 0, 0, null);
				messages.add(randomPromoPosition, promoMessage);
			}
		}

		runOnUiThread(new Runnable() {
			public void run() {
				setLoading(false);

				mProgressBarLoading.setVisibility(View.GONE);
				mPullToRefreshLayout.setVisibility(View.VISIBLE);
				mPullToRefreshLayout.setRefreshComplete();

				if (status == Server.STATUS_OK) {
					if (subscriptionUpdates >= 0) {
						mSubscriptionUpdates = subscriptionUpdates;
						invalidateOptionsMenu();
					}
					
					if (page == 0) {
						mAdapter.setItems(messages);
						mListView.setSelection(0);
					}
					else {
						mAdapter.addAll(messages);
					}
					
					// if we there are no more new items
					if (reachedEnd) {
						// disable infinite scrolling
						if (mInfiniteScrollListener != null) {
							mInfiniteScrollListener.setEnabled(false);
						}
					}
					
					mLastRefresh = System.currentTimeMillis();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					if (mAlertDialog != null && mAlertDialog.isShowing()) {
						mAlertDialog.dismiss();
					}
					
					// get the explanatory message and add a linked support email address
					final SpannableString verificationWhySpannable = new SpannableString(getString(R.string.verify_phone_number_why, Config.SUPPORT_EMAIL));
					Linkify.addLinks(verificationWhySpannable, Linkify.EMAIL_ADDRESSES);

					AlertDialog.Builder builder = new AlertDialog.Builder(AbstractMessagesActivity.this);
					builder.setTitle(R.string.verify_phone_number);
					builder.setMessage(verificationWhySpannable);
					builder.setCancelable(false);
					builder.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							onLoadVerification();
							Server.prepareVerification(AbstractMessagesActivity.this, AbstractMessagesActivity.this);
						}
					});
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// leave the Activity (and probably the whole app as well)
							finish();
						}
					});
					mAlertDialog = builder.show();

					// try to make the support email address in the dialog clickable
					try {
						((TextView) mAlertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
					}
					catch (Exception e) { }
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(AbstractMessagesActivity.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
				
				onUpdated(status, page);
			}
		});
	}

	@Override
	public void onSentMessage(int status, String messageText, final String messageTopic, String messageID, long messageTime, String messageColorHex, int messagePatternID, String messageCountryISO3) { }

	@Override
	public void onReceivedDetails(int status, boolean isFavorited, boolean isSubscribed) { }
	
	@Override
	public void onLoadVerification() {
		runOnUiThread(new Runnable() {
			public void run() {
				setLoading(true);
			}
		});
	}
	
	@Override
	public void onPreparedVerification(final int status, final String apiPhoneNumber, final String verificationCode, final long validUntil) {
		runOnUiThread(new Runnable() {
			public void run() {
				setLoading(false);
				if (status == Server.STATUS_OK) {
					if (mAlertDialog != null && mAlertDialog.isShowing()) {
						mAlertDialog.dismiss();
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(AbstractMessagesActivity.this);
					builder.setTitle(R.string.verify_phone_number);
					builder.setMessage(R.string.verify_phone_number_how);
					builder.setCancelable(false);
					builder.setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {						
							// create the verification text that contains the code
							final String verificationText = getVerificationMessageText(verificationCode);
							
							// build the SMS intent with pre-filled recipient and text
							try {
								Social.sendSMS(apiPhoneNumber, verificationText, R.string.verify_phone_number, AbstractMessagesActivity.this);
							}
							catch (Exception e) { }
							
							// close the application (the user may return later when the account is verified)
							finish();
						}
					});
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// leave the Activity (and probably the whole app as well)
							finish();
						}
					});
					mAlertDialog = builder.show();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					// irrelevant here
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(AbstractMessagesActivity.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(AbstractMessagesActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

}
