package com.example.smartwallet.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.smartwallet.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Та же мягкая серая тень, что у блока «Рекомендованная категория» на главной (API 28+).
 */
public final class AuthFieldCardsGlow {

    private AuthFieldCardsGlow() {}

    public static void applyToTree(@Nullable View root, @NonNull Context context) {
        if (root == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        int glow = ContextCompat.getColor(context, R.color.home_card_glow_shadow);
        applyRecursive(root, glow);
    }

    private static void applyRecursive(View v, int glow) {
        if (v instanceof MaterialCardView) {
            MaterialCardView c = (MaterialCardView) v;
            c.setOutlineAmbientShadowColor(glow);
            c.setOutlineSpotShadowColor(glow);
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                applyRecursive(g.getChildAt(i), glow);
            }
        }
    }
}
