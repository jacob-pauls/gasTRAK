package com.example.gastrak;

// Jacob Pauls
// 300273666
// GasTrakNotificationManager.java

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GasTrakNotificationManager {

    // hold name
    private static final String TAG = "GTNotificationManager";

    // hold context
    private Context context;
    // hold channel
    private int channel;
    // hold the id for retrieval
    private int notificationId;

    // hold notification channels
    private final String[] NOTIFICATION_CHANNEL_IDS = {"gasTRAKChannel1", "gasTRAKChannel2"};
    private final String[] NOTIFICATION_CHANNEL_NAMES = {"gasTRAK", "gasTRAK2"};

    public GasTrakNotificationManager(Context context, int channel) {
        this.context = context;
        createNotificationChannel(channel);
        this.channel = channel;
    }

    public int getLastNotificationId() {
        return notificationId;
    }

    public void sendNotification(String gasStation, String price) {
        String contentText = gasStation + " is now below $" + price + "/L!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_IDS[channel]);
            builder.setSmallIcon(R.drawable.ic_main_icon)
                    .setContentTitle(NOTIFICATION_CHANNEL_NAMES[channel])
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationId = generateID();
        notificationManager.notify(notificationId, builder.build());
    }

    public void sendDefaultNotification(String gasStation) {
        String contentText = "Check back on your custom notification for " + gasStation + "!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_IDS[channel]);
        builder.setSmallIcon(R.drawable.ic_main_icon)
                .setContentTitle(NOTIFICATION_CHANNEL_NAMES[channel])
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationId = generateID();
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(int channel) {
        // check API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // assign values for the notification
            CharSequence name = NOTIFICATION_CHANNEL_NAMES[channel];
            String description = "placeholder text";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // initialize channel
            NotificationChannel theChannel = new NotificationChannel(NOTIFICATION_CHANNEL_IDS[channel], name, importance);
            theChannel.setDescription(description);
            // register channel in the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(theChannel);
            Log.d(TAG, "createNotificationChannel: Notification channel created: " + theChannel.toString());
        }
    }

    private int generateID() {
        Date currentDate = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.CANADA).format(currentDate));
        Log.d(TAG, "generateID: Generated unique ID for new notification: " + id);
        return id;
    }

}
