package com.example.smartwallet.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class ResourceUtils {
    
    public static int getColor(Context context, int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorRes);
        } else {
            return ContextCompat.getColor(context, colorRes);
        }
    }
    
    public static Drawable getDrawable(Context context, int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableRes);
        } else {
            return ContextCompat.getDrawable(context, drawableRes);
        }
    }
    
    public static String getString(Context context, int stringRes) {
        return context.getString(stringRes);
    }
    
    public static String getString(Context context, int stringRes, Object... formatArgs) {
        return context.getString(stringRes, formatArgs);
    }
    
    public static int getDimensionPixelSize(Context context, int dimenRes) {
        return context.getResources().getDimensionPixelSize(dimenRes);
    }
    
    public static float getDimension(Context context, int dimenRes) {
        return context.getResources().getDimension(dimenRes);
    }
    
    public static int getInteger(Context context, int intRes) {
        return context.getResources().getInteger(intRes);
    }
    
    public static boolean getBoolean(Context context, int boolRes) {
        return context.getResources().getBoolean(boolRes);
    }
    
    public static int[] getIntArray(Context context, int arrayRes) {
        return context.getResources().getIntArray(arrayRes);
    }
    
    public static String[] getStringArray(Context context, int arrayRes) {
        return context.getResources().getStringArray(arrayRes);
    }
    
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static int getActionBarHeight(Context context) {
        int[] attrs = {android.R.attr.actionBarSize};
        android.content.res.TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs);
        int height = typedArray.getDimensionPixelSize(0, 0);
        typedArray.recycle();
        return height;
    }
    
    public static boolean isDarkTheme(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                           android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
    
    public static boolean isLightTheme(Context context) {
        return !isDarkTheme(context);
    }
    
    public static String getThemeName(Context context) {
        return isDarkTheme(context) ? "dark" : "light";
    }
}
