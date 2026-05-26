package com.example.smartwallet.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Случайные категории кэшбэка для карты: 3 разные категории из пула (~10), процент по каждой 1–10.
 */
public final class CashbackRulesGenerator {

    public static final String[] CATEGORY_POOL = {
            "еда",
            "транспорт",
            "здоровье",
            "развлечения",
            "одежда",
            "азс",
            "супермаркеты",
            "рестораны",
            "жкх",
            "путешествия"
    };

    private static final int PICK_COUNT = 3;

    private CashbackRulesGenerator() {}

    @NonNull
    public static LinkedHashMap<String, Integer> generate(@NonNull Random random) {
        List<Integer> idx = new ArrayList<>(CATEGORY_POOL.length);
        for (int i = 0; i < CATEGORY_POOL.length; i++) {
            idx.add(i);
        }
        Collections.shuffle(idx, random);
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < PICK_COUNT; i++) {
            String category = CATEGORY_POOL[idx.get(i)];
            int percent = 1 + random.nextInt(10);
            map.put(category, percent);
        }
        return map;
    }

    public static double averagePercent(@Nullable Map<String, Integer> rules) {
        if (rules == null || rules.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int v : rules.values()) {
            sum += v;
        }
        return (double) sum / rules.size();
    }
}
