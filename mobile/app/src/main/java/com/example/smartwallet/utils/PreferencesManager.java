package com.example.smartwallet.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "smartwallet_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_THEME = "theme";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_BIOMETRIC = "biometric";
    
    private static PreferencesManager instance;
    private SharedPreferences preferences;
    
    private PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    public void setFirstLaunch(boolean isFirstLaunch) {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch).apply();
    }
    
    public String getTheme() {
        return preferences.getString(KEY_THEME, "system");
    }
    
    public void setTheme(String theme) {
        preferences.edit().putString(KEY_THEME, theme).apply();
    }
    
    public boolean isNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }
    
    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
    
    public boolean isBiometricEnabled() {
        return preferences.getBoolean(KEY_BIOMETRIC, false);
    }
    
    public void setBiometricEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
    }
    
    public void clear() {
        preferences.edit().clear().apply();
    }
}
