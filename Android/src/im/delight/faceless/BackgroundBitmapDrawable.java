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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class BackgroundBitmapDrawable extends BitmapDrawable {

	public BackgroundBitmapDrawable(Resources resources, Bitmap bitmap) {
		super(resources, bitmap);
	}

	@Override
	public int getMinimumHeight() {
		return 0;
	}

	@Override
	public int getMinimumWidth() {
		return 0;
	}

}
