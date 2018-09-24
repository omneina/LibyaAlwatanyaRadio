package com.libyanelite.libyaalwatanyaradio;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AddChannelID extends Application {
    public static final String CHANNEL_ID = "Radio_Libya_Alwatanya";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Radio Libya Alwatanya",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Radio Libya Alwatanya Channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);

        }
    }
}
