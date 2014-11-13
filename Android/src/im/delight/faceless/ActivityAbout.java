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

import android.util.Base64;
import im.delight.android.baselib.Strings;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;

public class ActivityAbout extends Activity {
	
	private static final String URL_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=%s";
	private TextView mTextViewAbout;
	private Button mButtonRate;
	private Resources mResources;
	private static final String LINE_BREAK = "<br />";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		// set up resources reference
		mResources = getResources();
		
		// build and compose the about text
		StringBuilder builder = new StringBuilder();
		addStringsFromArray(builder, R.array.meta_about, LINE_BREAK+LINE_BREAK);
		addString(builder, LINE_BREAK);
		addStringsFromArray(builder, R.array.meta_questions_answers, LINE_BREAK+LINE_BREAK, true);
		addString(builder, LINE_BREAK);
		addTitleAndBody(builder, R.string.meta_security_title, R.array.meta_security_body);
		addTitleAndBody(builder, R.string.meta_permissions_title, R.array.meta_permissions_body);
		addTitleAndBody(builder, R.string.meta_privacy_title, R.array.meta_privacy_body);
		addTitleAndBody(builder, R.string.meta_open_source_title, R.array.meta_open_source_body);
		
		// remove 3 superfluous HTML line breaks at the end
		builder.setLength(builder.length() - (LINE_BREAK.length() * 3));
		
		// set up UI widgets
		if (Config.DEMO_ACTIVE) {
			// this outputs the app description (composed from single resources) to the console
			final StringBuilder appDescription = new StringBuilder();
			appDescription.append("<!-- APP DESCRIPTION -->\n");
			appDescription.append("<!-- BEGIN BASE64 -->\n");
			appDescription.append(Base64.encodeToString(builder.toString().getBytes(), Base64.NO_WRAP));
			appDescription.append("<!-- END BASE64 -->\n");

			final String[] descriptionChunks = Strings.splitToChunks(appDescription.toString(), 4000);
			for (String descriptionChunk : descriptionChunks) {
				System.out.println(descriptionChunk);
			}
		}
		mTextViewAbout = (TextView) findViewById(R.id.textViewAbout);
		mTextViewAbout.setText(Html.fromHtml(builder.toString()));
		mButtonRate = (Button) findViewById(R.id.buttonRate);
		mButtonRate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String packageName = getApplicationContext().getPackageName();
				Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URL_GOOGLE_PLAY, packageName)));
				startActivity(rateIntent);
			}
		});
		findViewById(R.id.buttonTranslate).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAbout.this);
				builder.setTitle(R.string.translate);
				builder.setMessage(R.string.translate_explanation);
				builder.setNeutralButton(R.string.ok, null);
				builder.show();
			}
		});
		
		// set up background color and pattern for the TextView
		BackgroundPatterns.applyRandomBackground(this, mTextViewAbout);

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, ActivitySettings.class)); // go one step up in Activity hierarchy
		finish(); // destroy this Activity so that the user does not immediately come back if they press "Back"
		return true;
	}
	
	private void addStringsFromArray(StringBuilder builder, int arrayResourceID, String separator) {
		addStringsFromArray(builder, arrayResourceID, separator, false);
	}
	
	private void addStringsFromArray(StringBuilder builder, int arrayResourceID, String separator, boolean isPairsWithHeadlines) {
		final String[] items = mResources.getStringArray(arrayResourceID);
		int counter = 0;
		for (String item : items) {
			if (isPairsWithHeadlines && counter % 2 == 0) {
				builder.append("<b>");
				builder.append(item);
				builder.append("</b>");
				builder.append("<br />");
			}
			else {
				builder.append(item);
				builder.append(separator);
			}
			counter++;
		}
	}
	
	private void addString(StringBuilder builder, int stringResourceID, boolean isHeadline) {
		if (isHeadline) {
			builder.append("<b>");
		}
		builder.append(getString(stringResourceID));
		if (isHeadline) {
			builder.append("</b>");
		}
	}
	
	private static void addString(StringBuilder builder, String string) {
		addString(builder, string, false);
	}
	
	private static void addString(StringBuilder builder, String string, boolean isHeadline) {
		if (isHeadline) {
			builder.append("<b>");
		}
		builder.append(string);
		if (isHeadline) {
			builder.append("</b>");
		}
	}
	
	private void addTitleAndBody(StringBuilder builder, int titleStringResID, int bodyArrayResID) {
		addString(builder, titleStringResID, true);
		addString(builder, LINE_BREAK);
		addStringsFromArray(builder, bodyArrayResID, LINE_BREAK);
		addString(builder, LINE_BREAK);
		addString(builder, LINE_BREAK);
	}

}
