package com.tgneventos.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreferences {

    private static final String PREFERENCES_NAME = "preferencias_usuario";
    private static final String KEY_DARK_MODE = "modo_oscuro";

    private ThemePreferences() {
    }

    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public static void applySavedNightMode(Context context) {
        if (isDarkModeEnabled(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
