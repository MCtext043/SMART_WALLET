package com.example.smartwallet.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    private static final Locale RUSSIAN_LOCALE = new Locale("ru", "RU");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(RUSSIAN_LOCALE);
    
    static {
        // Устанавливаем символ рубля
        CURRENCY_FORMAT.setCurrency(java.util.Currency.getInstance("RUB"));
    }
    
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    public static String formatCurrency(double amount, boolean showSign) {
        String formatted = CURRENCY_FORMAT.format(Math.abs(amount));
        if (showSign && amount != 0) {
            return (amount > 0 ? "+" : "-") + formatted;
        }
        return formatted;
    }
    
    public static String formatCurrencyShort(double amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fМ ₽", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.1fК ₽", amount / 1_000);
        } else {
            return String.format("%.0f ₽", amount);
        }
    }
    
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
}
