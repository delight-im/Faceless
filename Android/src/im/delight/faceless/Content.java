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

import android.text.format.DateUtils;

public abstract class Content {
	
	private static final long TIME_DISPLAY_LIMIT = DateUtils.HOUR_IN_MILLIS * 36;
	protected long mTime;

	public long getTime() {
		return mTime;
	}
	
	public boolean isTimeVisible() {
		return (System.currentTimeMillis() - getTime()) < TIME_DISPLAY_LIMIT;
	}

}
