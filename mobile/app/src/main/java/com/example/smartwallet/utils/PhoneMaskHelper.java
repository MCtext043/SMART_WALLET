package com.example.smartwallet.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Маска телефона: X-XXX-XXX-XX-XX (11 цифр, первая 7 или 8).
 */
public final class PhoneMaskHelper {

    public static final String HINT_FORMAT = "8-912-345-67-89";
    private static final int MAX_DIGITS = 11;

    private PhoneMaskHelper() {}

    @NonNull
    public static TextWatcher attach(@NonNull EditText editText) {
        TextWatcher watcher = new TextWatcher() {
            private boolean selfChange;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (selfChange) return;
                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > MAX_DIGITS) {
                    digits = digits.substring(0, MAX_DIGITS);
                }
                String formatted = formatDigits(digits);
                if (!formatted.contentEquals(s)) {
                    selfChange = true;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                    selfChange = false;
                }
            }
        };
        editText.addTextChangedListener(watcher);
        return watcher;
    }

    @NonNull
    private static String formatDigits(@NonNull String d) {
        if (d.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(d.charAt(0));
        int i = 1;
        if (i < d.length()) {
            sb.append('-');
            int e = Math.min(i + 3, d.length());
            sb.append(d, i, e);
            i = e;
        }
        if (i < d.length()) {
            sb.append('-');
            int e = Math.min(i + 3, d.length());
            sb.append(d, i, e);
            i = e;
        }
        if (i < d.length()) {
            sb.append('-');
            int e = Math.min(i + 2, d.length());
            sb.append(d, i, e);
            i = e;
        }
        if (i < d.length()) {
            sb.append('-');
            sb.append(d, i, d.length());
        }
        return sb.toString();
    }

    @NonNull
    public static String digitsOnly(String masked) {
        if (masked == null) return "";
        return masked.replaceAll("\\D", "");
    }

    /**
     * Отображение номера в профиле и т.п.: только цифры → маска X-XXX-XXX-XX-XX (до 11 цифр).
     */
    @NonNull
    public static String formatForDisplay(@Nullable String phone) {
        if (phone == null) return "";
        String d = digitsOnly(phone);
        if (d.isEmpty()) {
            return phone.trim();
        }
        if (d.length() > MAX_DIGITS) {
            d = d.substring(0, MAX_DIGITS);
        }
        return formatDigits(d);
    }

    public static boolean isCompleteValid(@NonNull String masked) {
        String d = digitsOnly(masked);
        if (d.length() != MAX_DIGITS) return false;
        char first = d.charAt(0);
        return first == '7' || first == '8';
    }
}
