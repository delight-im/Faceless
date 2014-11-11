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

import android.os.Parcel;
import android.os.Parcelable;

public class Comment extends Content implements Parcelable {

	private final String mID;
	private final String mText;
	private final int mPrivateRecipientInThread;
	private final boolean mIsAuthor;
	private final boolean mIsSelf;
	private final int mOwnerInThread;
	
	public Comment(String id, String text, int privateRecipientInThread, boolean isAuthor, boolean isSelf, int ownerInThread, long time) {
		mID = id;
		mText = text;
		mPrivateRecipientInThread = privateRecipientInThread;
		mIsAuthor = isAuthor;
		mIsSelf = isSelf;
		mOwnerInThread = ownerInThread;
		mTime = time;
	}
	
	public String getID() {
		return mID;
	}

	public String getText() {
		return mText;
	}
	
	public boolean isPrivate() {
		return mPrivateRecipientInThread != 0;
	}
	
	public int getPrivateRecipientInThread() {
		return mPrivateRecipientInThread;
	}

	public boolean isAuthor() {
		return mIsAuthor;
	}

	public boolean isSelf() {
		return mIsSelf;
	}
	
	public int getOwnerInThread() {
		return mOwnerInThread;
	}

	public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
		@Override
		public Comment createFromParcel(Parcel in) {
			return new Comment(in);
		}
		@Override
		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mID);
		out.writeString(mText);
		out.writeInt(mPrivateRecipientInThread);
		out.writeByte((byte) (mIsAuthor ? 1 : 0));
		out.writeByte((byte) (mIsSelf ? 1 : 0));
		out.writeInt(mOwnerInThread);
		out.writeLong(mTime);
	}

	private Comment(Parcel in) {
		mID = in.readString();
		mText = in.readString();
		mPrivateRecipientInThread = in.readInt();
		mIsAuthor = in.readByte() == 1;
		mIsSelf = in.readByte() == 1;
		mOwnerInThread = in.readInt();
		mTime = in.readLong();
	}

}
