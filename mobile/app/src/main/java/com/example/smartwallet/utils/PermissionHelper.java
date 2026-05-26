package com.example.smartwallet.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    
    public static final int PERMISSION_REQUEST_CODE = 1001;
    
    public static boolean hasAllRequiredPermissions(Context context) {
        return PermissionUtils.hasAllRequiredPermissions(context);
    }
    
    public static String[] getMissingRequiredPermissions(Context context) {
        return PermissionUtils.getMissingRequiredPermissions(context);
    }
    
    public static boolean hasNetworkPermissions(Context context) {
        return PermissionUtils.hasPermission(context, Manifest.permission.INTERNET) &&
               PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
    }
    
    public static void requestRequiredPermissions(Activity activity) {
        String[] missingPermissions = getMissingRequiredPermissions(activity);
        if (missingPermissions.length > 0) {
            ActivityCompat.requestPermissions(activity, missingPermissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    public static void requestNetworkPermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (!PermissionUtils.hasPermission(activity, Manifest.permission.INTERNET)) {
            permissionsToRequest.add(Manifest.permission.INTERNET);
        }
        
        if (!PermissionUtils.hasPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE)) {
            permissionsToRequest.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, 
                permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
    
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static String getPermissionRationale(String permission) {
        switch (permission) {
            case Manifest.permission.INTERNET:
                return "Приложению нужен доступ к интернету для загрузки данных";
            case Manifest.permission.ACCESS_NETWORK_STATE:
                return "Приложению нужно знать состояние сети для оптимизации работы";
            case Manifest.permission.ACCESS_WIFI_STATE:
                return "Приложению нужен доступ к информации о WiFi для лучшей работы";
            default:
                return "Это разрешение необходимо для корректной работы приложения";
        }
    }
    
    public static void handlePermissionResult(int requestCode, String[] permissions, 
                                           int[] grantResults, PermissionCallback callback) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            List<String> deniedPermissions = new ArrayList<>();
            
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    deniedPermissions.add(permissions[i]);
                }
            }
            
            if (callback != null) {
                if (allGranted) {
                    callback.onAllPermissionsGranted();
                } else {
                    callback.onPermissionsDenied(deniedPermissions.toArray(new String[0]));
                }
            }
        }
    }
    
    public interface PermissionCallback {
        void onAllPermissionsGranted();
        void onPermissionsDenied(String[] deniedPermissions);
    }
}
