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

import im.delight.android.progress.SimpleProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class ActivityIntro extends Activity {
	
	private static final String X_PERCENT = "%d %%";
	private Button mButtonNext;
	private TextView mTextViewIntroduction;
	private String[] mIntroductionSteps;
	private ProgressBar mProgressBarStep;
	private TextView mTextViewStep;
	private SharedPreferences mPrefs;
	private int mStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		// set up access to the preferences
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Global.Setup.load(mPrefs);
		
		// load the introduction steps
		mIntroductionSteps = getResources().getStringArray(R.array.introduction_steps);
		mStep = mPrefs.getInt(Global.Preferences.INTRO_STEP, 0);
		
		// make sure the step is valid
		if (mStep >= mIntroductionSteps.length) {
			mStep = 0;
		}
		
		// set up UI widgets
		mTextViewIntroduction = (TextView) findViewById(R.id.textViewIntroduction);
		mTextViewIntroduction.setMovementMethod(new ScrollingMovementMethod());
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		mProgressBarStep = (ProgressBar) findViewById(R.id.progressBarStep);
		mTextViewStep = (TextView) findViewById(R.id.textViewStep);
		updateTextAndProgress(mStep);

		// set up background color and pattern for the TextView
		BackgroundPatterns.applyRandomBackground(this, mTextViewIntroduction);
		
		// set up the button's OnClickListener
		mButtonNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// increase step counter
				mStep++;
				// if there are stil steps to be shown
				if (mStep < mIntroductionSteps.length) {
					saveStep(mStep);
					updateTextAndProgress(mStep);
				}
				// if introduction has been completed
				else {
					// show the loading indicator as we'll be doing some time-consuming work
					final SimpleProgressDialog loadingDialog = SimpleProgressDialog.show(ActivityIntro.this);
					// do the time-consuming work in another thread
					new Thread() {
						public void run() {
							// pre-run the setup here with a loading indicator so that the main screen remains responsive
							Global.Setup.load(mPrefs);
							if (!Global.Setup.isComplete()) {
								Global.Setup.runAuto(ActivityIntro.this, mPrefs);
							}

							runOnUiThread(new Runnable() {
								public void run() {
									// mark the introduction as completed
									saveStep(Integer.MAX_VALUE);

									// hide the loading indicator
									if (loadingDialog != null) {
										loadingDialog.dismiss();
									}

									// switch back to the main screen
									startActivity(new Intent(ActivityIntro.this, ActivityMain.class));
									// prevent any window animation because the user is to be redirected immediately
									overridePendingTransition(0, 0);
									finish();
								}
							});
						}
					}.start();
				}
			}
		});
	}
	
	private void saveStep(int step) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putInt(Global.Preferences.INTRO_STEP, step);
		editor.apply();
	}
	
	private void updateTextAndProgress(int step) {
		mTextViewIntroduction.setText(mIntroductionSteps[mStep]);
		
		int percentage = 100 * (mStep+1) / mIntroductionSteps.length;
		mProgressBarStep.setProgress(percentage);
		mTextViewStep.setText(String.format(X_PERCENT, percentage));
	}

}
