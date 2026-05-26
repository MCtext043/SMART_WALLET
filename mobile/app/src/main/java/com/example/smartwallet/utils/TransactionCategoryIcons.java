package com.example.smartwallet.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.smartwallet.R;

import java.util.Locale;

/**
 * Иконка категории для строки истории операций (плитка + картинка задаются в разметке / адаптере).
 */
public final class TransactionCategoryIcons {

    private TransactionCategoryIcons() {}

    @DrawableRes
    public static int getIconResId(@Nullable String category) {
        if (category == null || category.isEmpty()) {
            return R.drawable.ic_txn_category_other;
        }
        String c = category.toLowerCase(Locale.ROOT);

        if (containsAny(c, "еда", "food", "ресторан", "кафе")) {
            return R.drawable.ic_txn_category_food;
        }
        if (containsAny(c, "супермаркет")) {
            return R.drawable.ic_txn_category_shopping;
        }
        if (containsAny(c, "транспорт", "transport", "азс", "бензин", "авто")) {
            return R.drawable.ic_txn_category_transport;
        }
        if (containsAny(c, "здоровье", "health", "аптек", "медицин", "врач")) {
            return R.drawable.ic_txn_category_health;
        }
        if (containsAny(c, "развлечен", "entertainment", "кино", "игр")) {
            return R.drawable.ic_txn_category_entertainment;
        }
        if (containsAny(c, "покупк", "shopping", "одежд", "магазин")) {
            return R.drawable.ic_txn_category_shopping;
        }
        if (containsAny(c, "жкх", "коммунал", "квартплат")) {
            return R.drawable.ic_home;
        }
        if (containsAny(c, "путешеств", "travel", "авиа", "отель")) {
            return R.drawable.ic_calendar_24;
        }
        return R.drawable.ic_txn_category_other;
    }

    private static boolean containsAny(String normalizedLower, String... needles) {
        for (String n : needles) {
            if (normalizedLower.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
