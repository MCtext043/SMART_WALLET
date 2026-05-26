package com.example.smartwallet.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/([0-9]{2})$");
    
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        return CARD_NUMBER_PATTERN.matcher(cleanNumber).matches();
    }
    
    public static boolean isValidCVV(String cvv) {
        return cvv != null && CVV_PATTERN.matcher(cvv).matches();
    }
    
    public static boolean isValidExpiryDate(String expiry) {
        return expiry != null && EXPIRY_PATTERN.matcher(expiry).matches();
    }
    
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }
    
    public static boolean isValidAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }
    
    public static boolean isValidPercentage(double percentage) {
        return percentage >= 0 && percentage <= 100;
    }
    
    public static boolean isValidLimit(double limit) {
        return limit >= 0;
    }
    
    public static String cleanPhoneNumber(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+]", "");
    }
    
    public static String cleanCardNumber(String cardNumber) {
        if (cardNumber == null) return "";
        return cardNumber.replaceAll("[^0-9]", "");
    }
    
    public static String formatCardNumber(String cardNumber) {
        String clean = cleanCardNumber(cardNumber);
        if (clean.length() != 16) return cardNumber;
        
        return clean.substring(0, 4) + " " +
               clean.substring(4, 8) + " " +
               clean.substring(8, 12) + " " +
               clean.substring(12, 16);
    }
    
    public static String maskCardNumber(String cardNumber) {
        String clean = cleanCardNumber(cardNumber);
        if (clean.length() != 16) return cardNumber;
        
        return "**** **** **** " + clean.substring(12, 16);
    }
    
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return "Слабый";
        }
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.chars().anyMatch(Character::isUpperCase)) score++;
        if (password.chars().anyMatch(Character::isLowerCase)) score++;
        if (password.chars().anyMatch(Character::isDigit)) score++;
        if (password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0)) score++;
        
        if (score < 3) return "Слабый";
        if (score < 5) return "Средний";
        return "Сильный";
    }
}
