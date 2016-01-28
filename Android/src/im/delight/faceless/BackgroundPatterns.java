package im.delight.faceless;

/*
 * Copyright (c) delight.im <info@delight.im>
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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

public class BackgroundPatterns {
	
	private static BackgroundPatterns mInstance;
	private final Bitmap[] mBitmaps;
	
	private BackgroundPatterns(final Activity activity) {
		final Context context = activity.getApplicationContext();
		final List<Bitmap> bitmaps = new LinkedList<Bitmap>();
		
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_1));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_2));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_3));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_4));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_5));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_6));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_7));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_8));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_9));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_10));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_11));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_12));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_13));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_14));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_15));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_16));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_17));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_18));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_19));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_20));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_21));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_22));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_23));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_24));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_25));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_26));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_27));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_28));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_29));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_30));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_31));
		bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.pattern_32));
		
		mBitmaps = bitmaps.toArray(new Bitmap[bitmaps.size()]);
	}
	
	public static BackgroundPatterns getInstance(final Activity activity) {
		if (mInstance == null) {
			mInstance = new BackgroundPatterns(activity);
		}
		return mInstance;
	}
	
	public int getRandomPatternID() {
		Random random = new Random();
		return random.nextInt(mBitmaps.length);
	}
	
	public int validatePatternID(final int patternID) {
		return (patternID + mBitmaps.length) % mBitmaps.length;
	}
	
	public BitmapDrawable getBackgroundDrawable(final Context context, final int patternID, final int color) {
		BitmapDrawable out = new BackgroundBitmapDrawable(context.getResources(), mBitmaps[validatePatternID(patternID)]);
		out.setAntiAlias(true);
		out.setDither(true);
		out.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		out.setColorFilter(new PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY));
		return out;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void setViewBackground(final Context context, final View view, final int patternID, final int color) {
		BitmapDrawable drawable = getBackgroundDrawable(context, patternID, color);
		if (Build.VERSION.SDK_INT >= 16) {
			view.setBackground(drawable);
		}
		else {
			view.setBackgroundDrawable(drawable);
		}
	}
	
	public static void applyRandomBackground(final Activity activity, final View view) {
		final int backgroundColor = UI.getRandomColor();
		final BackgroundPatterns patterns = BackgroundPatterns.getInstance(activity);
		patterns.setViewBackground(activity, view, patterns.getRandomPatternID(), backgroundColor);
		if (view instanceof TextView) {
			((TextView) view).setTextColor(UI.getTextColor(backgroundColor));
			((TextView) view).setLinkTextColor(UI.getTextColor(backgroundColor));
		}
	}

}
