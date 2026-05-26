package com.example.smartwallet.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
    
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static File getAppDirectory(Context context) {
        return new File(context.getFilesDir(), "smartwallet");
    }
    
    public static File getCacheDirectory(Context context) {
        return new File(context.getCacheDir(), "smartwallet");
    }
    
    public static File getExternalAppDirectory(Context context) {
        if (!isExternalStorageAvailable()) {
            return null;
        }
        return new File(context.getExternalFilesDir(null), "smartwallet");
    }
    
    public static File getExternalCacheDirectory(Context context) {
        if (!isExternalStorageAvailable()) {
            return null;
        }
        return new File(context.getExternalCacheDir(), "smartwallet");
    }
    
    public static boolean createDirectory(File directory) {
        if (directory == null) return false;
        return directory.exists() || directory.mkdirs();
    }
    
    public static boolean createAppDirectories(Context context) {
        boolean success = true;
        success &= createDirectory(getAppDirectory(context));
        success &= createDirectory(getCacheDirectory(context));
        
        if (isExternalStorageAvailable()) {
            success &= createDirectory(getExternalAppDirectory(context));
            success &= createDirectory(getExternalCacheDirectory(context));
        }
        
        return success;
    }
    
    public static boolean copyFile(File source, File destination) {
        try {
            if (!source.exists()) return false;
            
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel destChannel = new FileOutputStream(destination).getChannel();
            
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            
            sourceChannel.close();
            destChannel.close();
            
            return true;
        } catch (IOException e) {
            Logger.e("FileUtils", "Error copying file", e);
            return false;
        }
    }
    
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) return true;
        
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFile(child);
                }
            }
        }
        
        return file.delete();
    }
    
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) return 0;
        
        if (file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            long size = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    size += getFileSize(child);
                }
            }
            return size;
        }
        
        return 0;
    }
    
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public static boolean clearCache(Context context) {
        boolean success = true;
        success &= deleteFile(getCacheDirectory(context));
        
        if (isExternalStorageAvailable()) {
            success &= deleteFile(getExternalCacheDirectory(context));
        }
        
        return success;
    }
    
    public static boolean clearAppData(Context context) {
        boolean success = true;
        success &= deleteFile(getAppDirectory(context));
        success &= deleteFile(getCacheDirectory(context));
        
        if (isExternalStorageAvailable()) {
            success &= deleteFile(getExternalAppDirectory(context));
            success &= deleteFile(getExternalCacheDirectory(context));
        }
        
        return success;
    }
}
