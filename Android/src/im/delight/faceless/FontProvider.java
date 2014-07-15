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

import android.content.Context;
import android.graphics.Typeface;

public class FontProvider {
	
	private static final String PATH_REGULAR = "fonts/Ubuntu-Regular.ttf";
	private static FontProvider mInstance;
	private Typeface mFontRegular;
	
	public static FontProvider getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FontProvider(context);
		}
		return mInstance;
	}
	
	private FontProvider(Context context) {
		mFontRegular = Typeface.createFromAsset(context.getAssets(), PATH_REGULAR);
	}
	
	public Typeface getFontRegular() {
		return mFontRegular;
	}

}
