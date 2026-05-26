package com.example.smartwallet.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.smartwallet.R;

import java.util.Random;

/**
 * Короткие «советы нейросети» для блока на главной — случайный выбор без запроса к API.
 */
public final class AiFinanceTips {

    private static final Random RANDOM = new Random();

    private AiFinanceTips() {}

    @NonNull
    public static String pickRandom(@NonNull Context context) {
        String[] tips = context.getResources().getStringArray(R.array.ai_finance_tips);
        if (tips.length == 0) {
            return "";
        }
        return tips[RANDOM.nextInt(tips.length)];
    }
}
