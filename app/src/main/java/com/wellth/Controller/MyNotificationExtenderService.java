package com.wellth.Controller;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;
import com.wellth.R;

import java.math.BigInteger;

public class MyNotificationExtenderService extends NotificationExtenderService {

    @Override
    protected boolean onNotificationProcessing(OSNotificationPayload notification) {

        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Red on Android 5.0+ devices.
                Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher);
                builder.setLargeIcon(icon);
                return builder.setSmallIcon(R.drawable.ic_stat_action_account_child);
//                return builder.setColor(new BigInteger("FFf82363", 16).intValue());
            }
        };
        String message = notification.message;
        if (message.contains("friend request")) {
            LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent intent = new Intent();
            intent.setAction(getResources().getString(R.string.update_request));
            mBroadcaster.sendBroadcast(intent);
        }

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + displayedResult.notificationId);

        return true;
    }
}
