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

import im.delight.android.tasks.RegularIntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;

public abstract class AbstractNotificationSender extends RegularIntentService {
	
	@SuppressWarnings("deprecation")
	protected void sendNotification(Class<?> targetActivityClass, int notificationID, String title, String body, int smallIconRes, int largeIconRes) {
		Intent resultIntent = new Intent(this, targetActivityClass);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(resultPendingIntent);
		builder.setSmallIcon(smallIconRes);
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIconRes));
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setContentTitle(title);
		builder.setContentText(body);
		builder.setLights(0xffffffff, 500, 2000);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(notificationID, builder.getNotification());
	}

}
