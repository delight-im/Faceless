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

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ActivityMain extends AbstractMessagesActivity {

	public static final String EXTRA_NEW_MESSAGE = "new_message";
	private static final int DEFAULT_MODE = Server.MODE_LATEST;
	// MODE SELECTION BEGIN
	private int mMode;
	private Spinner mSpinnerMode;
	private View mButtonSettings;
	// MODE SELECTION END

	@Override
	protected int getLayoutResourceID() {
		return R.layout.activity_main;
	}

	@Override
	protected int getMessagesMode() {
		return mMode;
	}

	private void changeMode(int mode) {
		mMode = mode;
		setActiveSpinnerPosition(mode);
		reloadMessages(mMode, 0, true, false, false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent != null) {
			final Message messageJustPublished = intent.<Message>getParcelableExtra(EXTRA_NEW_MESSAGE);
			if (messageJustPublished != null) {
				// add the newly published message to the top of the list
				mAdapter.insert(messageJustPublished, 0);
			}
		}
	}

	private static int getModeFromSpinnerPosition(final int spinnerPosition) {
		switch (spinnerPosition) {
			case 0: return Server.MODE_LATEST;
			case 1: return Server.MODE_NEARBY;
			case 2: return Server.MODE_FRIENDS;
			case 3: return Server.MODE_POPULAR;
			case 4: return Server.MODE_FAVORITES;
			default: throw new RuntimeException("Unknown spinner position: "+spinnerPosition);
		}
	}

	private static int getSpinnerPositionFromMode(final int mode) {
		switch (mode) {
			case Server.MODE_LATEST: return 0;
			case Server.MODE_NEARBY: return 1;
			case Server.MODE_FRIENDS: return 2;
			case Server.MODE_POPULAR: return 3;
			case Server.MODE_FAVORITES: return 4;
			default: throw new RuntimeException("Unknown mode: "+mode);
		}
	}

	@Override
	protected void setupButtonBar() {
		// set up the topic selection Spinner
		mSpinnerMode = (Spinner) findViewById(R.id.spinnerMode);
		final ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this, R.array.modes_human, R.layout.spinner_text_white);
		// show the Adapter's data in the Spinner
		modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerMode.setAdapter(modeAdapter);
		// set up the OnItemSelectedListener for this Spinner
		mSpinnerMode.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				final int newMode = getModeFromSpinnerPosition(position);
				if (newMode != mMode) {
					changeMode(newMode);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }

		});

		// get the settings button reference
		mButtonSettings = findViewById(R.id.buttonSettings);

		// set the initially selected mode
		mMode = DEFAULT_MODE;

		// set up the settings button OnClickListener
		mButtonSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ActivityMain.this, ActivitySettings.class));
			}
		});
	}

	private void setActiveSpinnerPosition(final int mode) {
		final int activePosition = getSpinnerPositionFromMode(mode);
		mSpinnerMode.setSelection(activePosition);
	}

	@Override
	public void onBackPressed() {
		// if we are already on the home tab
		if (mMode == DEFAULT_MODE) {
			// just perform the normal back key actions
			super.onBackPressed();
		}
		// if we are on another tab
		else {
			// first go back to the home tab
			changeMode(DEFAULT_MODE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				startActivity(new Intent(ActivityMain.this, ActivityAdd.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean isActionBarUpEnabled() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		View notificationCountView = menu.findItem(R.id.action_notification_count).getActionView();
		Button buttonNotificationCount = (Button) notificationCountView.findViewById(R.id.badge);
		buttonNotificationCount.setText(String.valueOf(mSubscriptionUpdates));
		if (mSubscriptionUpdates > 0) {
			buttonNotificationCount.setEnabled(true);
			buttonNotificationCount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(ActivityMain.this, ActivitySubscriptions.class));
					// destroy this Activity so that the subscription update count will be refreshed when returning
					finish();
				}
			});
		}
		else {
			buttonNotificationCount.setEnabled(false);
		}

		return true;
	}

	@Override
	protected void onUpdated(int status, int page) { }

	@Override
	protected void isScrollTop(boolean isAtTop) { }

}
