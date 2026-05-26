package com.example.smartwallet.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.smartwallet.network.dto.Card;

/**
 * Подпись карты для списков (банк и название продукта).
 */
public final class CardDisplayFormatter {

    private CardDisplayFormatter() {}

    @NonNull
    public static String bankAndCardName(@NonNull Card c) {
        String bank = c.bankName != null ? c.bankName.trim() : "";
        String name = c.cardName != null ? c.cardName.trim() : "";
        if (!TextUtils.isEmpty(bank) && !TextUtils.isEmpty(name)) {
            return bank + " · " + name;
        }
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        if (!TextUtils.isEmpty(bank)) {
            return bank;
        }
        return "Карта #" + c.id;
    }
}
