package com.example.smartwallet.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "smartwallet_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    /** Сохранять сессию между перезапусками приложения (иначе токен только в памяти процесса). */
    private static final String KEY_REMEMBER_ME = "remember_me";

    private static TokenManager instance;
    private SharedPreferences prefs;
    private Context context;
    /** Токен без «запомнить меня»: живёт до закрытия процесса, в SharedPreferences не пишется. */
    private volatile String memoryToken;
    
    private TokenManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Сохранить токен. При {@code rememberMe == false} сессия не переживёт полное закрытие приложения.
     */
    public void saveToken(String token, boolean rememberMe) {
        memoryToken = null;
        if (rememberMe) {
            prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, token)
                    .putBoolean(KEY_REMEMBER_ME, true)
                    .apply();
        } else {
            memoryToken = token;
            prefs.edit()
                    .remove(KEY_ACCESS_TOKEN)
                    .putBoolean(KEY_REMEMBER_ME, false)
                    .apply();
        }
    }

    /** То же, что {@link #saveToken(String, boolean) saveToken(token, true)}. */
    public void saveToken(String token) {
        saveToken(token, true);
    }

    public String getToken() {
        if (memoryToken != null) {
            return memoryToken;
        }
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void clearToken() {
        memoryToken = null;
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REMEMBER_ME)
                .apply();
    }
    
    public boolean hasToken() {
        return getToken() != null;
    }
    
    public Context getContext() {
        return context;
    }
}



