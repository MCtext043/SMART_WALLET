package com.example.smartwallet.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
    
    public static boolean isDeviceSecure(Context context) {
        // Simplified implementation - always return false for now
        return false;
    }
    
    public static boolean isDebugMode(Context context) {
        try {
            return (context.getApplicationInfo().flags & 
                   android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isRooted() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().close();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.e("SecurityUtils", "SHA-256 algorithm not found", e);
            return input;
        }
    }
    
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for x86") ||
               Build.MANUFACTURER.contains("Genymotion") ||
               (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
               "google_sdk".equals(Build.PRODUCT);
    }
    
    public static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
        return deviceId != null ? deviceId : "unknown";
    }
}
