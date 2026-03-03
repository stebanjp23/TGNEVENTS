package com.tgneventos.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public final class NotificationPreferences {

    private static final String TAG = "NotificationPrefs";
    private static final String PREFERENCES_NAME = "preferencias_usuario";
    public static final String KEY_NOTIFICATIONS = "notificaciones";
    public static final String GENERAL_TOPIC = "tgneventos_general";
    private static final String USERS_COLLECTION = "Usuarios";

    private NotificationPreferences() {
    }

    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public static boolean hasRuntimePermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean canDisplaySystemNotifications(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
                && hasRuntimePermission(context);
    }

    public static Task<Void> updateTopicSubscription(boolean enabled) {
        if (enabled) {
            return FirebaseMessaging.getInstance().subscribeToTopic(GENERAL_TOPIC);
        }
        return FirebaseMessaging.getInstance().unsubscribeFromTopic(GENERAL_TOPIC);
    }

    public static void syncTopicWithStoredSetting(Context context) {
        boolean enabled = isNotificationsEnabled(context);
        updateTopicSubscription(enabled).addOnFailureListener(e ->
                Log.w(TAG, "No se pudo sincronizar el topic de notificaciones", e));
        updateCurrentUserNotificationPreference(context);
    }

    public static void updateCurrentUserNotificationPreference(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("notificaciones_activas", isNotificationsEnabled(context));

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.w(TAG, "No se pudo guardar la preferencia de notificaciones", e));
    }

    public static void refreshAndStoreCurrentToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null && !token.isEmpty()) {
                        updateCurrentUserFcmToken(token);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo obtener el token FCM", e));
    }

    public static void updateCurrentUserFcmToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcm_token", token);

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo guardar el token FCM", e));
    }
}
