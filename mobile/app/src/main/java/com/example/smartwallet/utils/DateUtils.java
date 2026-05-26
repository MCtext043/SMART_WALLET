package com.example.smartwallet.utils;

import android.text.TextUtils;

import com.example.smartwallet.network.dto.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String[] API_DATE_FORMATS = new String[] {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"
    };
    private static final String DISPLAY_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    private static final String DISPLAY_DATE_ONLY_FORMAT = "dd.MM.yyyy";
    private static final String DISPLAY_TIME_ONLY_FORMAT = "HH:mm";
    
    public static String formatApiDate(String apiDate) {
        return formatApiDate(apiDate, DISPLAY_DATE_FORMAT);
    }

    /** Для списка операций: occurred_at, иначе created_at (как на бэкенде coalesce). */
    public static String formatTransactionDisplayDate(Transaction transaction) {
        if (transaction == null) return "—";
        String raw = !TextUtils.isEmpty(transaction.occurredAt) ? transaction.occurredAt : transaction.createdAt;
        if (TextUtils.isEmpty(raw)) return "—";
        return formatApiDateLenient(raw, DISPLAY_DATE_FORMAT);
    }

    /** Время для строки списка операций (над категорией). */
    public static String formatTransactionListTime(Transaction transaction) {
        if (transaction == null) return "—";
        String raw = !TextUtils.isEmpty(transaction.occurredAt) ? transaction.occurredAt : transaction.createdAt;
        if (TextUtils.isEmpty(raw)) return "—";
        return formatApiDateLenient(raw, DISPLAY_TIME_ONLY_FORMAT);
    }
    
    public static String formatApiDate(String apiDate, String outputFormat) {
        Date date = parseApiDateLenient(apiDate);
        if (date == null) {
            Logger.w("DateUtils", "Error parsing date: " + apiDate);
            return apiDate;
        }
        try {
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());
            return outputDateFormat.format(date);
        } catch (Exception e) {
            return apiDate;
        }
    }

    public static String formatApiDateLenient(String apiDate, String outputFormat) {
        Date date = parseApiDateLenient(apiDate);
        if (date == null) return apiDate;
        try {
            return new SimpleDateFormat(outputFormat, Locale.getDefault()).format(date);
        } catch (Exception e) {
            return apiDate;
        }
    }

    @androidx.annotation.Nullable
    public static Date parseApiDateLenient(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) return null;
        for (String pattern : API_DATE_FORMATS) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.US);
                inputFormat.setLenient(false);
                return inputFormat.parse(apiDate);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
    
    public static String formatDateOnly(String apiDate) {
        return formatApiDateLenient(apiDate, DISPLAY_DATE_ONLY_FORMAT);
    }
    
    public static String formatTimeOnly(String apiDate) {
        return formatApiDateLenient(apiDate, DISPLAY_TIME_ONLY_FORMAT);
    }
    
    public static boolean isToday(String apiDate) {
        Date transactionDate = parseApiDateLenient(apiDate);
        if (transactionDate == null) return false;
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(transactionDate).equals(dateFormat.format(today));
    }
    
    public static boolean isYesterday(String apiDate) {
        Date transactionDate = parseApiDateLenient(apiDate);
        if (transactionDate == null) return false;
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(transactionDate).equals(dateFormat.format(yesterday));
    }
}
