package com.tgneventos.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tgneventos.R;
import com.tgneventos.model.Login;
import com.tgneventos.util.NotificationPreferences;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public final class TgnFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "tgneventos_general_channel";
    private static final String CHANNEL_NAME = "TGN Eventos";
    private static final String CHANNEL_DESCRIPTION = "Avisos y recordatorios de eventos";
    private static final String DEFAULT_TITLE = "TGN Eventos";
    private static final String DEFAULT_BODY = "Tienes una nueva notificacion";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        NotificationPreferences.updateCurrentUserFcmToken(token);
        NotificationPreferences.syncTopicWithStoredSetting(this);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!NotificationPreferences.isNotificationsEnabled(this)) {
            return;
        }

        if (!NotificationPreferences.canDisplaySystemNotifications(this)) {
            return;
        }

        String title = DEFAULT_TITLE;
        String body = DEFAULT_BODY;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
                title = notification.getTitle();
            }
            if (notification.getBody() != null && !notification.getBody().isEmpty()) {
                body = notification.getBody();
            }
        }

        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("title") && data.get("title") != null && !data.get("title").isEmpty()) {
            title = data.get("title");
        }
        if (data.containsKey("body") && data.get("body") != null && !data.get("body").isEmpty()) {
            body = data.get("body");
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        createNotificationChannelIfNeeded();

        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify((int) (System.currentTimeMillis() & 0x0FFFFFFF), notificationBuilder.build());
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(CHANNEL_DESCRIPTION);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
