package com.example.smartwallet.ui;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.smartwallet.R;
import com.example.smartwallet.network.dto.Card;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Внешний вид карточки по названию банка (и при необходимости продукта).
 */
public final class BankCardStyle {

    @DrawableRes
    public final int backgroundRes;
    @ColorRes
    public final int primaryTextColorRes;
    @ColorRes
    public final int secondaryTextColorRes;

    private BankCardStyle(@DrawableRes int backgroundRes,
                          @ColorRes int primaryTextColorRes,
                          @ColorRes int secondaryTextColorRes) {
        this.backgroundRes = backgroundRes;
        this.primaryTextColorRes = primaryTextColorRes;
        this.secondaryTextColorRes = secondaryTextColorRes;
    }

    @NonNull
    public static BankCardStyle forCard(@NonNull Card card) {
        String blob = (nz(card.bankName) + " " + nz(card.cardName)).toLowerCase(Locale.forLanguageTag("ru"));

        // Сбер
        if (matches(blob, "сбер", "sber", "сбербанк")) {
            return new BankCardStyle(R.drawable.bg_bank_card_sber,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Т-Банк / Тинькофф
        if (matches(blob, "тиньк", "tinkoff", "т-банк", "т банк", "tbank", "t-bank")) {
            return new BankCardStyle(R.drawable.bg_bank_card_tinkoff,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Альфа
        if (matches(blob, "альф", "alfa", "alfabank")) {
            return new BankCardStyle(R.drawable.bg_bank_card_alfa,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // ВТБ
        if (matches(blob, "втб", "vtb")) {
            return new BankCardStyle(R.drawable.bg_bank_card_vtb,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Райффайзен
        if (matches(blob, "райф", "raif", "raiffeisen")) {
            return new BankCardStyle(R.drawable.bg_bank_card_raiffeisen,
                    R.color.bank_card_text_on_light, R.color.bank_card_text_on_light_muted);
        }
        // Газпромбанк
        if (matches(blob, "газпром", "gazprom")) {
            return new BankCardStyle(R.drawable.bg_bank_card_gazprom,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Открытие
        if (matches(blob, "открыт", "otkritie", "открытие")) {
            return new BankCardStyle(R.drawable.bg_bank_card_otkritie,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // МТС Банк
        if (matches(blob, "мтс", "mts")) {
            return new BankCardStyle(R.drawable.bg_bank_card_mts,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Озон
        if (matches(blob, "озон", "ozon")) {
            return new BankCardStyle(R.drawable.bg_bank_card_ozon,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // Яндекс
        if (matches(blob, "яндекс", "yandex")) {
            return new BankCardStyle(R.drawable.bg_bank_card_yandex,
                    R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
        }
        // По умолчанию — фирменный зелёный SmartWallet
        return new BankCardStyle(R.drawable.bg_bank_card_default,
                R.color.bank_card_text_on_dark, R.color.bank_card_text_on_dark_muted);
    }

    public void apply(@NonNull MaterialCardView cardView) {
        // MaterialCardView не наследует CardView — фон задаём как у View + прозрачная заливка карты.
        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        cardView.setBackgroundResource(backgroundRes);
    }

    public int primaryTextColor(@NonNull Context context) {
        return ContextCompat.getColor(context, primaryTextColorRes);
    }

    public int secondaryTextColor(@NonNull Context context) {
        return ContextCompat.getColor(context, secondaryTextColorRes);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean matches(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }
}
