package com.example.smartwallet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (SecurityException e) {
            Logger.w("NetworkUtils", "No permission to check network state, assuming network is available");
            // If we don't have permission, assume network is available
            // This prevents crashes while still allowing the app to function
            return true;
        } catch (Exception e) {
            Logger.e("NetworkUtils", "Error checking network availability", e);
        }
        
        return false;
    }
    
    public static boolean isWifiConnected(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return wifiInfo != null && wifiInfo.isConnected();
            }
        } catch (SecurityException e) {
            Logger.w("NetworkUtils", "No permission to check WiFi state");
        } catch (Exception e) {
            Logger.e("NetworkUtils", "Error checking WiFi connection", e);
        }
        
        return false;
    }
    
    public static boolean isMobileConnected(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                return mobileInfo != null && mobileInfo.isConnected();
            }
        } catch (SecurityException e) {
            Logger.w("NetworkUtils", "No permission to check mobile state");
        } catch (Exception e) {
            Logger.e("NetworkUtils", "Error checking mobile connection", e);
        }
        
        return false;
    }
    
    public static String getConnectionType(Context context) {
        if (isWifiConnected(context)) {
            return "WiFi";
        } else if (isMobileConnected(context)) {
            return "Mobile";
        } else {
            return "Unknown";
        }
    }
}
