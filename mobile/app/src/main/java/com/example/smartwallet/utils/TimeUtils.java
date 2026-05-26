package com.example.smartwallet.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm";
    public static final String API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    public static String formatDate(Date date) {
        return formatDate(date, DATE_FORMAT);
    }
    
    public static String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }
    
    public static String formatTime(Date date) {
        return formatDate(date, TIME_FORMAT);
    }
    
    public static String formatDateTime(Date date) {
        return formatDate(date, DATETIME_FORMAT);
    }
    
    public static String getCurrentDate() {
        return formatDate(new Date());
    }
    
    public static String getCurrentTime() {
        return formatTime(new Date());
    }
    
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }
    
    public static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }
    
    public static boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        
        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }
    
    public static boolean isThisWeek(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               now.get(Calendar.WEEK_OF_YEAR) == target.get(Calendar.WEEK_OF_YEAR);
    }
    
    public static boolean isThisMonth(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               now.get(Calendar.MONTH) == target.get(Calendar.MONTH);
    }
    
    public static boolean isThisYear(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR);
    }
    
    public static long getDaysBetween(Date start, Date end) {
        long diffInMillies = end.getTime() - start.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    public static long getHoursBetween(Date start, Date end) {
        long diffInMillies = end.getTime() - start.getTime();
        return TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    public static long getMinutesBetween(Date start, Date end) {
        long diffInMillies = end.getTime() - start.getTime();
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    public static String getRelativeTime(Date date) {
        long minutes = getMinutesBetween(date, new Date());
        
        if (minutes < 1) {
            return "только что";
        } else if (minutes < 60) {
            return minutes + " мин назад";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " ч назад";
        } else {
            long days = minutes / 1440;
            return days + " дн назад";
        }
    }
}
