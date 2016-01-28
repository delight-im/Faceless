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

import android.content.Context;
import im.delight.android.baselib.Social;
import android.app.Activity;
import android.os.Bundle;

public class ActivityInvite extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inviteFriends();
		finish();
	}

	private void inviteFriends() {
		// prepare the message
		final String subject = getString(R.string.invitation_title);
		final String body = subject + "\n\n" + Config.SHARE_URL + "\n";

		// ask how to send the invitation
		sendInviteMessage(this, R.string.action_invite, subject, body);
	}

	private static void sendInviteMessage(final Context context, final int windowCaptionRes, final String invitationSubject, final String invitationBody) {
		try {
			Social.sendSMS(null, invitationBody, windowCaptionRes, context);
		}
		catch (Exception e) {
			Social.shareText(context, context.getString(windowCaptionRes), invitationBody, invitationSubject);
		}
	}

}
