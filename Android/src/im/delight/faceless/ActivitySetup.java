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

import im.delight.android.baselib.Phone;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class ActivitySetup extends Activity {
	
	private EditText mEditTextPhoneNumber;
	private ProgressBar mProgressBarChecking;
	private Button mButtonNext;
	private View.OnClickListener mButtonGetStartedClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setLoading(true);
			
			// check the input and run the setup in a new thread
			new Thread() {
				public void run() {
					final String phoneNumber = mEditTextPhoneNumber.getText().toString().trim();
					final String countryIso2 = Phone.getCountry(ActivitySetup.this, "US");
					final String[] validatedPhoneData = Global.PhoneWithLib.normalizeNumber(phoneNumber, countryIso2);

					// show the results back on the UI thread
					runOnUiThread(new Runnable() {
						public void run() {
							// if a valid phone number has been entered
							if (validatedPhoneData != null) {
								String validatedPhoneNumber = validatedPhoneData[0];
								String validatedPhoneRegionCode = validatedPhoneData[1];
								
								if (validatedPhoneNumber != null && validatedPhoneNumber.length() > 0) {
									if (validatedPhoneRegionCode != null && validatedPhoneRegionCode.length() > 0) {
										// save the input and finish the manual setup
										Global.Setup.runManually(validatedPhoneData, PreferenceManager.getDefaultSharedPreferences(ActivitySetup.this));
										
										// switch back to main screen
										startActivity(new Intent(ActivitySetup.this, ActivityMain.class));
										// prevent any window animation because the user is to be redirected immediately
										overridePendingTransition(0, 0);
										finish();
									}
									// phone number was not valid
									else {
										// let the user enter a phone number again (try again)
										setLoading(false);
									}
								}
								// phone number was not valid
								else {
									// let the user enter a phone number again (try again)
									setLoading(false);
								}
							}
							// phone number was not valid
							else {
								// let the user enter a phone number again (try again)
								setLoading(false);
							}
						}
					});
				}
			}.start();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		
		// set up resources
		Global.Setup.load(PreferenceManager.getDefaultSharedPreferences(ActivitySetup.this));
		
		// set up UI widgets
		final TextView textViewSetupIntro = (TextView) findViewById(R.id.textViewSetupIntro);
		textViewSetupIntro.setMovementMethod(new ScrollingMovementMethod());
		mEditTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
		mProgressBarChecking = (ProgressBar) findViewById(R.id.progressBarChecking);
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		
		// clear the hint when the user enters the EditText
		mEditTextPhoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mEditTextPhoneNumber.setHint(null);
				}
			}
		});

		// set up background color and pattern for the TextView
		BackgroundPatterns.applyRandomBackground(this, textViewSetupIntro);
		
		// set up the button's OnClickListener
		mButtonNext.setOnClickListener(mButtonGetStartedClick);
	}
	
	private void setLoading(boolean loading) {
		if (loading) {
			mButtonNext.setOnClickListener(null);
			mEditTextPhoneNumber.setEnabled(false);
			mProgressBarChecking.setVisibility(View.VISIBLE);
		}
		else {
			mProgressBarChecking.setVisibility(View.INVISIBLE);
			mEditTextPhoneNumber.setEnabled(true);
			mButtonNext.setOnClickListener(mButtonGetStartedClick);
		}
	}

}
