package com.example.smartwallet.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class ConfigUtils {
    
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }
    
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
    
    public static float getDensity(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }
    
    public static int getDensityDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi;
    }
    
    public static boolean isHighDensity(Context context) {
        return getDensityDpi(context) >= DisplayMetrics.DENSITY_HIGH;
    }
    
    public static boolean isXHighDensity(Context context) {
        return getDensityDpi(context) >= DisplayMetrics.DENSITY_XHIGH;
    }
    
    public static boolean isXXHighDensity(Context context) {
        return getDensityDpi(context) >= DisplayMetrics.DENSITY_XXHIGH;
    }
    
    public static boolean isXXXHighDensity(Context context) {
        return getDensityDpi(context) >= DisplayMetrics.DENSITY_XXXHIGH;
    }
    
    public static int dpToPx(Context context, int dp) {
        return (int) (dp * getDensity(context));
    }
    
    public static int pxToDp(Context context, int px) {
        return (int) (px / getDensity(context));
    }
    
    public static boolean isRTL(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == 
               android.view.View.LAYOUT_DIRECTION_RTL;
    }
    
    public static String getLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }
    
    public static String getCountry(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }
}
