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

import im.delight.android.baselib.Social;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivityInvite extends Activity {
	
	// VIEWS BEGIN
	private TextView mTextViewData;
	private Button mButtonNext;
	// VIEWS END
	// DATA BEGIN
	private boolean mIsFirstScreen;
	private String mInvitationText;
	// DATA END
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite);

		// set up the Views
		mTextViewData = (TextView) findViewById(R.id.textViewData);
		mTextViewData.setMovementMethod(new ScrollingMovementMethod());
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		
		// prepare the invitation text
		final String[] metaAboutArray = getResources().getStringArray(R.array.meta_about);
		StringBuilder invitationText = new StringBuilder();
		invitationText.append(metaAboutArray[0]);
		invitationText.append("\n\n");
		invitationText.append(metaAboutArray[1]);
		invitationText.append("\n\n");
		invitationText.append(metaAboutArray[2]);
		invitationText.append("\n\n");
		invitationText.append(Config.SHARE_URL);
		invitationText.append("\n");
		mInvitationText = invitationText.toString();

		// set up the action buttons at the bottom
		mButtonNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// if we're in the first step (continue to preview)
				if (mIsFirstScreen) {
					setFirstScreen(false);
				}
				// if we're in the second step (continue to send)
				else {
					// offer a selection of all apps that can send the invitation text
					Social.shareText(ActivityInvite.this, getString(R.string.action_invite), mInvitationText, getString(R.string.invitation_title));
					// leave the invitation page again
					finish();
				}
			}
		});
		
		// initialize the first screen
		setFirstScreen(true);

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, ActivitySettings.class)); // go one step up in Activity hierarchy
		finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
		return true;
	}
	
	private void setFirstScreen(boolean first) {
		if (first) {
			mTextViewData.setText(R.string.invitation_howto);
			mButtonNext.setText(R.string.continueToPreview);
			setTitle(getString(R.string.action_invite));

			// set up the TextView style
			mTextViewData.setBackgroundColor(getResources().getColor(R.color.background_darkest));
			mTextViewData.setTextColor(Color.WHITE);
			mTextViewData.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

			mIsFirstScreen = true;
		}
		else {
			mTextViewData.setText(mInvitationText);
			mButtonNext.setText(R.string.choose_recipient);
			setTitle(getString(R.string.preview));

			// set up the TextView style
			BackgroundPatterns.applyRandomBackground(this, mTextViewData);
			mTextViewData.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

			mIsFirstScreen = false;
		}
		// scroll back to the top of the TextView
		mTextViewData.scrollTo(0, 0);
	}
	
	@Override
	public void onBackPressed() {
		if (mIsFirstScreen) {
			super.onBackPressed();
		}
		else {
			setFirstScreen(true);
		}
	}

}
