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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class ActivitySubscriptions extends AbstractMessagesActivity implements Server.Callback.SubscriptionEvent {

	private Button mButtonClearAll;
	
	public static class MessageReadReceiver {

		private static MessageReadReceiver mInstance;
		private ArrayAdapter<Message> mMessagesAdapter;
		
		private MessageReadReceiver() {
			mMessagesAdapter = null;
		}
		
		public static MessageReadReceiver getInstance() {
			if (mInstance == null) {
				mInstance = new MessageReadReceiver();
			}
			return mInstance;
		}
		
		public void setAdapter(ArrayAdapter<Message> adapter) {
			mMessagesAdapter = adapter;
		}
		
		public void setMessageRead(Message message) {
			if (mMessagesAdapter != null) {
				// remove the message from the list of unread messages
				mMessagesAdapter.remove(message);
			}
		}

	}

	@Override
	protected int getLayoutResourceID() {
		return R.layout.activity_subscriptions;
	}

	@Override
	protected int getMessagesMode() {
		return Server.MODE_SUBSCRIPTIONS;
	}

	@Override
	protected void setupButtonBar() {
		mButtonClearAll = (Button) findViewById(R.id.buttonClearAll);		
		mButtonClearAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setLoading(true);
				Server.clearSubscriptions(ActivitySubscriptions.this, ActivitySubscriptions.this);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, ActivityMain.class)); // go one step up in Activity hierarchy
		finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, ActivityMain.class)); // go one step up in Activity hierarchy
		finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
		return true;
	}

	@Override
	protected boolean isActionBarUpEnabled() {
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// listen for new messages read on the current Adapter
		MessageReadReceiver.getInstance().setAdapter(mAdapter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// stop listening for new messages read on the current Adapter
		MessageReadReceiver.getInstance().setAdapter(null);
	}

	@Override
	public void onChangedSubscription(int status, boolean subscribed) { }

	@Override
	public void onClearedSubscriptions(final int status) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLoading(false);

				if (status == Server.STATUS_OK) {
					startActivity(new Intent(ActivitySubscriptions.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_MAINTENANCE) {
					Toast.makeText(ActivitySubscriptions.this, R.string.error_maintenance, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_BAD_REQUEST) {
					Toast.makeText(ActivitySubscriptions.this, R.string.error_bad_request, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_OUTDATED_CLIENT) {
					Toast.makeText(ActivitySubscriptions.this, R.string.error_outdated_client, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_NOT_AUTHORIZED) {
					startActivity(new Intent(ActivitySubscriptions.this, ActivityMain.class));
					finish();
				}
				else if (status == Server.STATUS_TEMPORARILY_BANNED) {
					Toast.makeText(ActivitySubscriptions.this, R.string.error_temporarily_banned, Toast.LENGTH_SHORT).show();
				}
				else if (status == Server.STATUS_LOGIN_THROTTLED) {
					mAlertDialog = Global.showLoginThrottledInfo(ActivitySubscriptions.this);
				}
				else if (status == Server.STATUS_NO_CONNECTION) {
					Toast.makeText(ActivitySubscriptions.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onUpdated(int status, int page) { }

	@Override
	protected void isScrollTop(boolean isAtTop) { }

}
