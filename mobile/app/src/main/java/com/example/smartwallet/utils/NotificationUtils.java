package com.example.smartwallet.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationUtils {
    
    public static final String CHANNEL_ID_TRANSACTIONS = "transactions";
    public static final String CHANNEL_ID_CASHBACK = "cashback";
    public static final String CHANNEL_ID_REMINDERS = "reminders";
    public static final String CHANNEL_ID_GENERAL = "general";
    
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Transaction notifications
                NotificationChannel transactionChannel = new NotificationChannel(
                    CHANNEL_ID_TRANSACTIONS,
                    "Транзакции",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                transactionChannel.setDescription("Уведомления о транзакциях");
                notificationManager.createNotificationChannel(transactionChannel);
                
                // Cashback notifications
                NotificationChannel cashbackChannel = new NotificationChannel(
                    CHANNEL_ID_CASHBACK,
                    "Кэшбэк",
                    NotificationManager.IMPORTANCE_HIGH
                );
                cashbackChannel.setDescription("Уведомления о кэшбэке");
                notificationManager.createNotificationChannel(cashbackChannel);
                
                // Reminder notifications
                NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Напоминания",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                reminderChannel.setDescription("Напоминания и уведомления");
                notificationManager.createNotificationChannel(reminderChannel);
                
                // General notifications
                NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "Общие",
                    NotificationManager.IMPORTANCE_LOW
                );
                generalChannel.setDescription("Общие уведомления");
                notificationManager.createNotificationChannel(generalChannel);
            }
        }
    }
    
    public static NotificationCompat.Builder createNotificationBuilder(
            Context context, String channelId, String title, String content) {
        return new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
    }
    
    public static NotificationCompat.Builder createTransactionNotification(
            Context context, String title, String content) {
        return createNotificationBuilder(context, CHANNEL_ID_TRANSACTIONS, title, content);
    }
    
    public static NotificationCompat.Builder createCashbackNotification(
            Context context, String title, String content) {
        return createNotificationBuilder(context, CHANNEL_ID_CASHBACK, title, content);
    }
    
    public static NotificationCompat.Builder createReminderNotification(
            Context context, String title, String content) {
        return createNotificationBuilder(context, CHANNEL_ID_REMINDERS, title, content);
    }
    
    public static NotificationCompat.Builder createGeneralNotification(
            Context context, String title, String content) {
        return createNotificationBuilder(context, CHANNEL_ID_GENERAL, title, content);
    }
    
    public static void showNotification(Context context, int notificationId, 
                                      NotificationCompat.Builder builder) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
    
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }
    
    public static void cancelAllNotifications(Context context) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
    
    public static boolean areNotificationsEnabled(Context context) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            return notificationManager.areNotificationsEnabled();
        }
        
        return false;
    }
    
    public static boolean isChannelEnabled(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
        }
        
        return true;
    }
}
