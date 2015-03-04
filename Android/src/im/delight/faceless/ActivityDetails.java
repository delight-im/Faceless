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
import im.delight.android.baselib.Social;
import im.delight.android.baselib.UI;
import im.delight.android.baselib.ViewScreenshot;
import im.delight.android.progress.SimpleProgressDialog;
import im.delight.android.time.RelativeTime;
import im.delight.faceless.ActivitySubscriptions.MessageReadReceiver;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;

public class ActivityDetails extends Activity implements OnRefreshListener, Server.Callback.MessageEvent, Server.Callback.CommentEvent, Server.Callback.FavoriteEvent, Server.Callback.ConnectionEvent, Server.Callback.ReportEvent, Server.Callback.SubscriptionEvent, ViewScreenshot.Callback {

	public static final String EXTRA_MESSAGE = "message";
	private static final String SHARED_IMAGE_FILENAME = "Faceless";
	private static final int REPORT_WRONG_TOPIC = 0;
	private static final int REPORT_BULLYING_HARASSMENT = 1;
	private static final int REPORT_VIOLENCE_THREATS = 2;
	private static final int REPORT_HATE_SPEECH = 3;
	private static final int REPORT_OBSCENE_CONTENT = 4;
	private static final int REPORT_SPAM_ADVERTISING = 5;
	private static final int REPORT_FRAUD_FALSEHOOD = 6;
	private static final int REPORT_ILLEGAL_CONTENT = 7;
	private static final int REPORT_BLOCK_AUTHOR = 8;
	private static final int MAX_CHARS_COMMENT = 2400;
	private LayoutInflater mInflater;
	private Resources mResources;
	private Global.MessagePropertyDrawables mMessagePropertyDrawables;
	// TWO MAIN VIEW GROUPS BEGIN
	private View mViewScreenshotContainer;
	private View mViewListViewContainer;
	// TWO MAIN VIEW GROUPS BEGIN
	private TextView mTextViewMessage;
	private TextView mTextViewTopic;
	private TextView mTextViewFavoritesOrTime;
	private TextView mTextViewDegree;
	private TextView mTextViewComments;
	// BUTTONS FOR SWITCHING BETWEEN MESSAGE AND COMMENTS BEGIN
	private Button mButtonMessage;
	private Button mButtonComments;
	// BUTTONS FOR SWITCHING BETWEEN MESSAGE AND COMMENTS END
	// LISTVIEW WITH ADAPTER AND PROGRESSBAR BEGIN
	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView mListView;
	private CommentsAdapter mAdapter;
	// LISTVIEW WITH ADAPTER AND PROGRESSBAR END
	private Message mMessage;
	private SimpleProgressDialog mSimpleProgressDialog;
	private AlertDialog mAlertDialog;
	private int mMessageDetailsStatus;
	private SimpleLocation mSimpleLocation;
	// COMMENTS SCREEN BEGIN
	private EditText mEditTextComment;
	private ImageButton mImageButtonCommentSubmit;
	private boolean mConfirmPublicReplies;
	// COMMENTS SCREEN END

	private void updateMessage(Intent intent) {
		Message message = null;
		try {
			message = intent.<Message>getParcelableExtra(EXTRA_MESSAGE);
		}
		catch (Exception e) { }

		if (message != null) {
			mMessage = message;

			// update contents
			int textColor = UI.getTextColor(mMessage.getColor());
			mViewScreenshotContainer.setBackgroundColor(mMessage.getColor());
			mTextViewMessage.setText(AndroidEmoji.ensure(mMessage.getText(), ActivityDetails.this));
			if (mMessage.getTopic() != null && mMessage.getTopic().length() > 0) {
				mTextViewTopic.setText(mMessage.getTopicText(this));
				mTextViewTopic.setVisibility(View.VISIBLE);
			}
			else {
				mTextViewTopic.setVisibility(View.GONE);
			}
			if (mMessage.isTimeVisible()) {
				mTextViewFavoritesOrTime.setText(RelativeTime.fromTimestamp(mResources, mMessage.getTime()));
				mTextViewFavoritesOrTime.setVisibility(View.VISIBLE);
			}
			else {
				mTextViewFavoritesOrTime.setVisibility(View.GONE);
			}
			mTextViewDegree.setText(mMessage.getOriginIndicator(this, mSimpleLocation.getPosition()));
			updateFavoritesAndComments();

			// update text and shadow colors
			mTextViewMessage.setTextColor(textColor);
			mTextViewMessage.setLinkTextColor(textColor);
			mTextViewTopic.setTextColor(textColor);
			mTextViewFavoritesOrTime.setTextColor(textColor);
			mTextViewDegree.setTextColor(textColor);
			mTextViewComments.setTextColor(textColor);

			// update property images (TextViews' compound drawables)
			mTextViewTopic.setCompoundDrawablesWithIntrinsicBounds(mMessagePropertyDrawables.getTopic(textColor == Color.BLACK), null, null, null);
			if (mMessage.isTimeVisible()) {
				mTextViewFavoritesOrTime.setCompoundDrawablesWithIntrinsicBounds(null, null, mMessagePropertyDrawables.getTime(textColor == Color.BLACK), null);
			}
			else {
				mTextViewFavoritesOrTime.setCompoundDrawablesWithIntrinsicBounds(null, null, mMessagePropertyDrawables.getFavorites(textColor == Color.BLACK), null);
			}
			mTextViewDegree.setCompoundDrawablesWithIntrinsicBounds(mMessagePropertyDrawables.getDegree(textColor == Color.BLACK), null, null, null);
			mTextViewComments.setCompoundDrawablesWithIntrinsicBounds(null, null, mMessagePropertyDrawables.getComments(textColor == Color.BLACK), null);

			// set background texture
			BackgroundPatterns backgroundPatterns = BackgroundPatterns.getInstance(this);
			backgroundPatterns.setViewBackground(this, mViewScreenshotContainer, mMessage.getPatternID(), mMessage.getColor());
			mTextViewMessage.setTypeface(FontProvider.getInstance(ActivityDetails.this).getFontRegular());

			// load the message's details before we let the user interact with the message
			mMessageDetailsStatus = 0;
			Server.getMessageDetails(this, mMessage.getID(), this);

			// slowly fade in the text
			Global.UI.fadeIn(mTextViewMessage);
		}
	}

	private void updateFavoritesAndComments() {
		if (!mMessage.isTimeVisible()) {
			mTextViewFavoritesOrTime.setText(mMessage.getFavorites() > 0 ? mResources.getQuantityString(R.plurals.x_favorites, mMessage.getFavorites(), mMessage.getFavorites()) : "");
			mTextViewFavoritesOrTime.setVisibility(mMessage.getFavorites() > 0 ? View.VISIBLE : View.GONE);
		}
		if (mMessage.isReasonForBan()) {
			mTextViewComments.setText(R.string.reason_for_ban);
		}
		else {
			mTextViewComments.setText(mResources.getQuantityString(R.plurals.x_comments, mMessage.getComments(), mMessage.getComments()));
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateMessage(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		// set up the layout inflater
		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		// set up resources
		Global.Setup.load(PreferenceManager.getDefaultSharedPreferences(ActivityDetails.this));
		mResources = getResources();
		mMessagePropertyDrawables = new Global.MessagePropertyDrawables(this);

		// set up two main view groups
		mViewScreenshotContainer = findViewById(R.id.viewScreenshotContainer);
		mViewListViewContainer = findViewById(R.id.viewListViewContainer);

		// set up UI widgets
		mTextViewMessage = (TextView) findViewById(R.id.textViewMessage);
		mTextViewTopic = (TextView) findViewById(R.id.textViewTopic);
		mTextViewFavoritesOrTime = (TextView) findViewById(R.id.textViewFavoritesOrTime);
		mTextViewDegree = (TextView) findViewById(R.id.textViewDegree);
		mTextViewComments = (TextView) findViewById(R.id.textViewComments);
		mButtonMessage = (Button) findViewById(R.id.buttonMessage);
		mButtonComments = (Button) findViewById(R.id.buttonComments);
		mEditTextComment = (EditText) findViewById(R.id.editTextComment);
		mImageButtonCommentSubmit = (ImageButton) findViewById(R.id.imageButtonCommentSubmit);

		// set up a length filter for the EditText
		UI.setMaxLength(mEditTextComment, MAX_CHARS_COMMENT);

		// set up the buttons' OnClickListeners
		mButtonMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessageView();
			}
		});
		mButtonComments.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewScreenshotContainer.setVisibility(View.GONE);
				mViewListViewContainer.setVisibility(View.VISIBLE);

				mButtonComments.setEnabled(false);
				mButtonMessage.setEnabled(true);

				reloadComments(true);
			}
		});
		mImageButtonCommentSubmit.setEnabled(true);
		mImageButtonCommentSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendCommentFromView(mEditTextComment, null);
			}
		});

		// set up the ListView with its ArrayAdapter and the ProgressBar
		mListView = (ListView) findViewById(R.id.listViewComments);
		mAdapter = new CommentsAdapter(this, R.layout.row_comments_list, new ArrayList<Comment>());
		mListView.setAdapter(mAdapter);

		// set up the reporting menu for the comments (in the long-click listener)
		mListView.setLongClickable(true);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Comment comment;
				try {
					comment = mAdapter.getItem(position);
				}
				catch (Exception e) {
					return;
				}
				if (comment == null) {
					return;
				}
				if (!comment.isSelf()) {
					showCommentOptions(comment);
				}
			}
		});

		// set up pull-to-refresh (at the top)
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pullToRefreshLayout);
		ActionBarPullToRefresh.from(this).allChildrenArePullable().listener(this).setup(mPullToRefreshLayout);

		// create the location provider
		mSimpleLocation = new SimpleLocation(this);

		// get the message
		updateMessage(getIntent());

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		UI.forceOverflowMenu(this);
	}

	private void sendCommentFromView(final EditText source, final Comment privateReplyTarget) {
		final String text = Emoji.replaceInText(source.getText().toString().trim());
		if (text.length() > 0) {
			// if we need to confirm that this comment is to be public
			if (mConfirmPublicReplies && privateReplyTarget == null) {
				// ask to decide between public and private reply first
				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetails.this);
				builder.setTitle(R.string.comment_confirm_public_title);
				builder.setMessage(R.string.comment_confirm_public_body);
				builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// only publish after successful confirmation
						sendComment(text, privateReplyTarget);
					}
				});
				builder.setNegativeButton(R.string.cancel, null);
				mAlertDialog = builder.show();
			}
			// if we don't need to confirm that this comment is to be public
			else {
				// just publish it
				sendComment(text, privateReplyTarget);
			}
		}
	}

	private void sendComment(final String text, final Comment privateReplyTarget) {
		mImageButtonCommentSubmit.setEnabled(false);
		setLoading(true);

		final String privateReplyTo = privateReplyTarget == null ? null : privateReplyTarget.getID();
		Server.addComment(ActivityDetails.this, mMessage.getID(), privateReplyTo, text, ActivityDetails.this);
	}

	private void showCommentOptions(final Comment comment) {
		final CharSequence[] options = { getString(R.string.reply_privately), getString(R.string.report_comment) };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.comment_options);
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					replyPrivately(comment);
				}
				else if (which == 1) {
					reportContent("comment", comment.getID());
				}
			}
		});
		builder.setNeutralButton(R.string.cancel, null);
		mAlertDialog = builder.show();
	}

	private void replyPrivately(final Comment comment) {
		final View viewReply = View.inflate(this, R.layout.dialog_reply_privately, null);

		final TextView textViewOriginalComment = (TextView) viewReply.findViewById(R.id.textViewOriginalComment);
		// show the original comment for reference
		textViewOriginalComment.setText(AndroidEmoji.ensure(comment.getText(), ActivityDetails.this));
		final EditText editTextPrivateReply = (EditText) viewReply.findViewById(R.id.editTextPrivateReply);
		// re-use any public comment that has been drafted already
		editTextPrivateReply.setText(mEditTextComment.getText().toString());
		UI.putCursorToEnd(editTextPrivateReply);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.reply_privately);
		builder.setView(viewReply);
		builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendCommentFromView(editTextPrivateReply, comment);
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		mAlertDialog = builder.show();
	}

	@Override
	public void onBackPressed() {
		// if we are already on the home tab
		if (mViewScreenshotContainer.getVisibility() == View.VISIBLE) {
			// just perform the normal back key actions
			super.onBackPressed();
		}
		// if we are on another tab
		else {
			// first go back to the home tab
			showMessageView();
		}
	}

	private void showMessageView() {
		mViewListViewContainer.setVisibility(View.GONE);
		mViewScreenshotContainer.setVisibility(View.VISIBLE);

		mButtonMessage.setEnabled(false);
		mButtonComments.setEnabled(true);

		updateFavoritesAndComments();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.details, menu);

		// if the message details have not been received yet
		if (mMessageDetailsStatus == 0) {
			// show a loading indicator (progress bar) first
			menu.findItem(R.id.action_loading).setVisible(true).setEnabled(true);
			menu.findItem(R.id.action_add_favorite).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_remove_favorite).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_add_subscription).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_remove_subscription).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_share_picture).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_report).setVisible(false).setEnabled(false);

			// show the menu
			return true;
		}
		// if the message details have been successfully received
		else if (mMessageDetailsStatus == Server.STATUS_OK) {
			// show the actual menu
			final boolean isFavorited;
			final boolean isSubscribed;
			final boolean isOwnMessage;
			if (mMessage != null) {
				isFavorited = mMessage.isFavorited();
				isSubscribed = mMessage.isSubscribed();
				isOwnMessage = mMessage == null || mMessage.getDegree() <= 0;
			}
			else {
				isFavorited = false;
				isSubscribed = false;
				isOwnMessage = false;
			}

			menu.findItem(R.id.action_loading).setVisible(false).setEnabled(false);
			menu.findItem(R.id.action_add_favorite).setVisible(!isFavorited).setEnabled(!isFavorited);
			menu.findItem(R.id.action_remove_favorite).setVisible(isFavorited).setEnabled(isFavorited);
			menu.findItem(R.id.action_add_subscription).setVisible(!isSubscribed).setEnabled(!isSubscribed);
			menu.findItem(R.id.action_remove_subscription).setVisible(isSubscribed).setEnabled(isSubscribed);
			menu.findItem(R.id.action_share_picture).setVisible(!isOwnMessage).setEnabled(!isOwnMessage);
			menu.findItem(R.id.action_report).setVisible(!isOwnMessage).setEnabled(!isOwnMessage);

			// show the menu
			return true;
		}
		// if the message details could not be received
		else {
			// hide the menu
			return false;
		}
	}

	private void reportAffirmation(final int reportOption, final CharSequence optionTitle, final String contentType, final String contentID) {
		final int affirmationMessage;
		final int affirmationConfirm;
		final Runnable affirmationAction;

		if (reportOption == REPORT_WRONG_TOPIC || reportOption == REPORT_BULLYING_HARASSMENT || reportOption == REPORT_VIOLENCE_THREATS || reportOption == REPORT_HATE_SPEECH || reportOption == REPORT_OBSCENE_CONTENT || reportOption == REPORT_SPAM_ADVERTISING || reportOption == REPORT_FRAUD_FALSEHOOD || reportOption == REPORT_ILLEGAL_CONTENT) {
			affirmationMessage = R.string.report_warning_report;
			affirmationConfirm = R.string.report_confirm_report;
			affirmationAction = new Runnable() {

				@Override
				public void run() {
					Server.sendReport(ActivityDetails.this, contentType, contentID, reportOption, ActivityDetails.this);
				}

			};
		}
		else if (reportOption == REPORT_BLOCK_AUTHOR) {
			affirmationMessage = R.string.report_warning_block;
			affirmationConfirm = R.string.report_confirm_block;
			affirmationAction = new Runnable() {

				@Override
				public void run() {
					Server.setBlocked(ActivityDetails.this, contentType, contentID, ActivityDetails.this);
				}

			};
		}
		else {
			throw new RuntimeException("Unknown reporting option: "+reportOption);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetails.this);
		builder.setTitle(optionTitle);
		builder.setMessage(affirmationMessage);
		builder.setPositiveButton(affirmationConfirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setLoading(true);
				affirmationAction.run();
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		mAlertDialog = builder.show();
	}

	private void reportContent(final String contentType, final String contentID) {
		final String[] options = getResources().getStringArray(R.array.report_options);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (contentType.equals("message")) {
			builder.setTitle(R.string.action_report);
		}
		else if (contentType.equals("comment")) {
			builder.setTitle(R.string.report_comment);
		}
		else {
			throw new RuntimeException("Unknown content type: "+contentType);
		}
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which < options.length) {
					reportAffirmation(which, options[which], contentType, contentID);
				}
			}
		});
		builder.setNeutralButton(R.string.cancel, null);
		mAlertDialog = builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_favorite:
				setLoading(true);
				Server.setFavorited(ActivityDetails.this, mMessage.getID(), true, ActivityDetails.this);
				return true;
			case R.id.action_remove_favorite:
				setLoading(true);
				Server.setFavorited(ActivityDetails.this, mMessage.getID(), false, ActivityDetails.this);
				return true;
			case R.id.action_share_picture:
				setLoading(true);
				new ViewScreenshot(ActivityDetails.this, ActivityDetails.this).from(mViewScreenshotContainer).asFile(SHARED_IMAGE_FILENAME).build();
				return true;
			case R.id.action_add_subscription:
				setLoading(true);
				Server.setSubscribed(ActivityDetails.this, mMessage.getID(), true, ActivityDetails.this);
				return true;
			case R.id.action_remove_subscription:
				setLoading(true);
				Server.setSubscribed(ActivityDetails.this, mMessage.getID(), false, ActivityDetails.this);
				return true;
			case R.id.action_report:
				reportContent("message", mMessage.getID());
				return true;
			default:
				startActivity(new Intent(this, ActivityMain.class)); // go one step up in Activity hierarchy
				finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
				return true;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// if location access is enabled
		if (mSimpleLocation.hasLocationEnabled()) {
			// ask the device to update the location
			mSimpleLocation.beginUpdates();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// stop requesting and receiving updates for the location
		mSimpleLocation.endUpdates();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
		setLoading(false);
	}

	public static class CommentsAdapterViews {
		public ImageView identiconAuthor;
		public ImageView identiconPrivateRecipient;
		public View viewCommentContainer;
		public TextView textViewComment;
		public TextView textViewTime;
	}

	public class CommentsAdapter extends ArrayAdapter<Comment> {

		public CommentsAdapter(Context context, int textViewResourceId, List<Comment> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setItems(Collection<? extends Comment> items) {
			clear();
			addAll(items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CommentsAdapterViews holder;
			View v = convertView;
			final Comment o = getItem(position);
			if (v == null) { // view must be initialized (it is a new one)
				v = mInflater.inflate(R.layout.row_comments_list, parent, false);
				holder = new CommentsAdapterViews();
				holder.identiconAuthor = (ImageView) v.findViewById(R.id.identiconAuthor);
				holder.identiconPrivateRecipient = (ImageView) v.findViewById(R.id.identiconPrivateRecipient);
				holder.viewCommentContainer = v.findViewById(R.id.viewCommentContainer);
				holder.textViewComment = (TextView) v.findViewById(R.id.textViewComment);
				holder.textViewComment.setTypeface(FontProvider.getInstance(ActivityDetails.this).getFontRegular());
				holder.textViewTime = (TextView) v.findViewById(R.id.textViewTime);
				v.setTag(holder);
			}
			else { // view is already initialized (old view could be recycled)
				holder = (CommentsAdapterViews) v.getTag();
			}
			if (o != null) {
				// update contents
				holder.identiconAuthor.setImageResource(Identicon.forComment(o.getOwnerInThread(), mMessage.getTime()));
				if (o.isPrivate()) {
					holder.identiconPrivateRecipient.setImageResource(Identicon.forComment(o.getPrivateRecipientInThread(), mMessage.getTime()));
					holder.identiconPrivateRecipient.setVisibility(View.VISIBLE);
				}
				else {
					holder.identiconPrivateRecipient.setVisibility(View.GONE);
				}
				if (o.isSelf()) {
					holder.viewCommentContainer.setBackgroundColor(Color.argb(60, 255, 255, 255));
					if (o.isPrivate()) {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(getString(R.string.degree_self)+"\n"+getString(R.string.private_reply)+"\n"+RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText(getString(R.string.degree_self)+"\n"+getString(R.string.private_reply));
						}
					}
					else {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(getString(R.string.degree_self)+"\n"+RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText(getString(R.string.degree_self));
						}
					}
					holder.textViewTime.setVisibility(View.VISIBLE);
				}
				else if (o.isAuthor()) {
					holder.viewCommentContainer.setBackgroundColor(Color.argb(100, 255, 0, 0));
					if (o.isPrivate()) {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(getString(R.string.degree_author)+"\n"+getString(R.string.private_reply)+"\n"+RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText(getString(R.string.degree_author)+"\n"+getString(R.string.private_reply));
						}
					}
					else {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(getString(R.string.degree_author)+"\n"+RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText(getString(R.string.degree_author));
						}
					}
					holder.textViewTime.setVisibility(View.VISIBLE);
				}
				else {
					holder.viewCommentContainer.setBackgroundColor(Color.argb(20, 255, 255, 255));
					if (o.isPrivate()) {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(getString(R.string.private_reply)+"\n"+RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText(getString(R.string.private_reply));
						}
					}
					else {
						if (o.isTimeVisible()) {
							holder.textViewTime.setText(RelativeTime.fromTimestamp(mResources, o.getTime()));
						}
						else {
							holder.textViewTime.setText("");
						}
					}
					holder.textViewTime.setVisibility((o.isPrivate() || o.isTimeVisible()) ? View.VISIBLE : View.GONE);
				}
				holder.textViewComment.setText(AndroidEmoji.ensure(o.getText(), ActivityDetails.this));
			}
			return v;
		}

	}

	private void reloadComments(boolean showLoadingDialog) {
		if (showLoadingDialog) {
			setLoading(true);
		}
		Server.getComments(this, mMessage.getID(), this);
	}

	@Override
	public void onRefreshStarted(View view) {
		reloadComments(false);
	}

	private void setLoading(boolean loading) {
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

	private void setMessageFavorited(boolean state) {
		if (mMessage != null) {
			mMessage.setFavorited(state);
		}
	}

	private void setMessageSubscribed(boolean state) {
		if (mMessage != null) {
			mMessage.setSubscribed(state);
		}
	}

	@Override
	public void onSentReport(final int status) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				if (status == Server.STATUS_OK) {
					Toast.makeText(ActivityDetails.this, R.string.report_sent, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onAddedFriend(final int status) { }

	@Override
	public void onAddedBlock(final int status) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				if (status == Server.STATUS_OK) {
					Toast.makeText(ActivityDetails.this, R.string.block_user_added, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onChangedFavorite(final int status, final boolean favorited) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				if (status == Server.STATUS_OK) {
					setMessageFavorited(favorited);
					mMessage.updateFavorites(favorited ? 1 : -1);
					updateFavoritesAndComments();
					invalidateOptionsMenu();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onReceivedComments(final int status, final List<Comment> comments, final boolean hasPrivateComments) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				mPullToRefreshLayout.setRefreshComplete();
				if (status == Server.STATUS_OK) {
					mAdapter.setItems(comments);

					// tell other components that a message has just been read (if any component is listening)
					MessageReadReceiver.getInstance().setMessageRead(mMessage);

					// if there are private comments in the thread already double check before replying publicly
					mConfirmPublicReplies = hasPrivateComments;
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onSentComment(final int status, final String commentText, final String commentID, final int ownerInThread, final int privateRecipientInThread, final long commentTime) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				mImageButtonCommentSubmit.setEnabled(true);
				if (status == Server.STATUS_OK) {
					UI.setKeyboardVisibility(ActivityDetails.this, mEditTextComment, false);
					mEditTextComment.setText("");

					// only if the comment is public
					if (privateRecipientInThread == 0) {
						mMessage.increaseComments();
					}

					// after commenting automatically subscribe to new comments (if not done already)
					setMessageSubscribed(true);
					invalidateOptionsMenu();

					// insert a preview of the comment right at the top
					final Comment publishedComment = new Comment(commentID, commentText, privateRecipientInThread, false, true, ownerInThread, commentTime);
					mAdapter.insert(publishedComment, 0);
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onReceivedMessages(int status, int mode, int page, boolean reachedEnd, long latestMessageID, int subscriptionUpdates, List<Message> messages) { }

	@Override
	public void onReceivedDetails(final int status, final boolean isFavorited, final boolean isSubscribed) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mMessageDetailsStatus = status;
				if (status == Server.STATUS_OK) {
					setMessageFavorited(isFavorited);
					setMessageSubscribed(isSubscribed);
					invalidateOptionsMenu();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onSentMessage(int status, String messageText, final String messageTopic, String messageID, long messageTime, String messageColorHex, int messagePatternID, String messageCountryISO3) { }

	@Override
	public void onChangedSubscription(final int status, final boolean subscribed) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);
				if (status == Server.STATUS_OK) {
					setMessageSubscribed(subscribed);
					invalidateOptionsMenu();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivityDetails.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivityDetails.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivityDetails.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivityDetails.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivityDetails.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivityDetails.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivityDetails.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	public void onClearedSubscriptions(int status) { }

	@Override
	public void onSuccess(File file) {
		setLoading(false);
		Social.shareFile(ActivityDetails.this, getString(R.string.action_share_picture), file, "image/png", getString(R.string.app_name));
	}

	@Override
	public void onError() {
		setLoading(false);
		Toast.makeText(ActivityDetails.this, R.string.share_image_failed, Toast.LENGTH_SHORT).show();
	}

}