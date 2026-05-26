package com.example.smartwallet.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionUtils {
    
    public static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    };
    
    public static final String[] RECOMMENDED_PERMISSIONS = {
        Manifest.permission.ACCESS_WIFI_STATE
    };
    
    public static final String[] OPTIONAL_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean hasAllRequiredPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }
    
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    
    public static String[] getMissingRequiredPermissions(Context context) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (!hasPermission(context, permission)) {
                missing.add(permission);
            }
        }
        return missing.toArray(new String[0]);
    }
}
