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

import im.delight.android.baselib.UI;
import com.larswerkman.holocolorpicker.ColorPicker;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class ActivityCustomize extends Activity implements ColorPicker.OnColorSelectedListener {
	
	public static final String EXTRA_COLOR = "color";
	public static final String EXTRA_PATTERN_ID = "patternID";
	public static final String EXTRA_TEXT = "text";
	// COMPONENTS FOR CHOOSING THE BACKGROUND STYLE BEGIN
	private ColorPicker mColorPickerBackground;
	private TextView mTextViewPatternPickerCaption;
	// COMPONENTS FOR CHOOSING THE BACKGROUND STYLE END
	private Button mButtonSave;
	private BackgroundPatterns mBackgroundPatterns;
	private int mColor;
	private int mPatternID;
	private String mText;
	
	private void updateTextAndColor(Intent intent) {
		int color = 0;
		int patternID = -1;
		String text = null;
		try {
			color = intent.getIntExtra(EXTRA_COLOR, 0);
			patternID = intent.getIntExtra(EXTRA_PATTERN_ID, -1);
			text = intent.getStringExtra(EXTRA_TEXT);
		}
		catch (Exception e) { }
		
		if (color != 0) {
			mColor = color;
		}
		if (patternID != -1) {
			mPatternID = patternID;
		}
		if (text != null) {
			mText = text;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		updateTextAndColor(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customize);
		
		// set up resources
		Global.Setup.load(PreferenceManager.getDefaultSharedPreferences(ActivityCustomize.this));
		mBackgroundPatterns = BackgroundPatterns.getInstance(this);

		// set default values for message properties
		mColor = UI.getRandomColor();
		mPatternID = mBackgroundPatterns.getRandomPatternID();
		mText = "";

		// get color and text that may have been previously defined
		updateTextAndColor(getIntent());
		
		// set up the components for choosing the background style
		mColorPickerBackground = (ColorPicker) findViewById(R.id.colorPickerBackground);
		mColorPickerBackground.setShowOldCenterColor(false);
		mColorPickerBackground.setColor(mColor);
		mTextViewPatternPickerCaption = (TextView) findViewById(R.id.textViewPatternPickerCaption);
		
		
		// show the initial preview of color and pattern
		showPatternPreview(mPatternID, mColor);
		
		// set up the color picker listener
		mColorPickerBackground.setOnColorSelectedListener(this);
		
		// set up the pattern picker listener
		mTextViewPatternPickerCaption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// forward to the next background pattern
				mPatternID = mBackgroundPatterns.validatePatternID(mPatternID+1);
				showPatternPreview(mPatternID, mColor);
			}
		});
		
		// set up the button for saving and going back to the Activity "Add"
		mButtonSave = (Button) findViewById(R.id.buttonSave);
		mButtonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// send the user back to the Activity "Add" with the updated color and unchanged text
				Intent intentBack = new Intent(ActivityCustomize.this, ActivityAdd.class);
				intentBack.putExtra(ActivityAdd.EXTRA_COLOR, mColor);
				intentBack.putExtra(ActivityAdd.EXTRA_PATTERN_ID, mPatternID);
				intentBack.putExtra(ActivityAdd.EXTRA_TEXT, mText);
				startActivity(intentBack);
				
				// destroy this Activity because everything is finished here
				finish();
			}
		});

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void showPatternPreview(int patternID, int color) {
		mBackgroundPatterns.setViewBackground(ActivityCustomize.this, mTextViewPatternPickerCaption, patternID, color);
		mTextViewPatternPickerCaption.setTextColor(UI.getTextColor(mColor));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish(); // destroy this Activity to go back to the "Add" Activity
		return true;
	}

	@Override
	public void onColorSelected(int color) {
		mColor = color;
		showPatternPreview(mPatternID, mColor);
	}

}
