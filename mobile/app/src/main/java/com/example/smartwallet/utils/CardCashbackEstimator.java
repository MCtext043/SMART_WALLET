package com.example.smartwallet.utils;

import androidx.annotation.NonNull;

import com.example.smartwallet.network.dto.Card;

import java.util.Locale;
import java.util.Map;

/**
 * Оценка процента и суммы кэшбэка по правилам карты для категории платежа (без учёта лимитов банка).
 */
public final class CardCashbackEstimator {

    private CardCashbackEstimator() {}

    /** Процент кэшбэка по категории или 0. */
    public static int percentForCategory(@NonNull Card card, @NonNull String categoryKey) {
        Map<String, Integer> rules = card.cashbackRules;
        if (rules == null || rules.isEmpty()) {
            return 0;
        }
        String cat = categoryKey.toLowerCase(Locale.forLanguageTag("ru"));
        for (Map.Entry<String, Integer> e : rules.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            if (e.getKey().toLowerCase(Locale.forLanguageTag("ru")).equals(cat)) {
                return e.getValue();
            }
        }
        for (Map.Entry<String, Integer> e : rules.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            String k = e.getKey().toLowerCase(Locale.forLanguageTag("ru"));
            if (k.contains(cat) || cat.contains(k)) {
                return e.getValue();
            }
        }
        Integer prochee = rules.get("прочее");
        if (prochee != null) {
            return prochee;
        }
        for (Map.Entry<String, Integer> e : rules.entrySet()) {
            if (e.getKey() != null && e.getKey().toLowerCase(Locale.forLanguageTag("ru")).equals("прочее")) {
                return e.getValue() != null ? e.getValue() : 0;
            }
        }
        return 0;
    }

    public static double cashbackRubForAmount(@NonNull Card card, @NonNull String categoryKey, double amountRub) {
        int p = percentForCategory(card, categoryKey);
        return amountRub * p / 100.0;
    }
}
