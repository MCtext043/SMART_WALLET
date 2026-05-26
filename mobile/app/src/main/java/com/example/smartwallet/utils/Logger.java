package com.example.smartwallet.utils;

import android.util.Log;

public class Logger {
    private static final String TAG = "SmartWallet";
    private static final boolean DEBUG = true; // В продакшене должно быть false
    
    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
    
    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(TAG + ":" + tag, message);
        }
    }
    
    public static void i(String message) {
        Log.i(TAG, message);
    }
    
    public static void i(String tag, String message) {
        Log.i(TAG + ":" + tag, message);
    }
    
    public static void w(String message) {
        Log.w(TAG, message);
    }
    
    public static void w(String tag, String message) {
        Log.w(TAG + ":" + tag, message);
    }
    
    public static void e(String message) {
        Log.e(TAG, message);
    }
    
    public static void e(String tag, String message) {
        Log.e(TAG + ":" + tag, message);
    }
    
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + ":" + tag, message, throwable);
    }
}
