package com.example.smartwallet.ui;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Визуально приподнимает выбранный пункт (как «кружок» над панелью) и сбрасывает остальные.
 */
public final class BottomNavSelectionHelper {

    private BottomNavSelectionHelper() {}

    public static void applyLift(@NonNull BottomNavigationView nav, @IdRes int selectedItemId) {
        BottomNavigationMenuView menuView = findMenuView(nav);
        if (menuView == null) return;
        menuView.setClipChildren(false);
        menuView.setClipToPadding(false);

        Menu menu = nav.getMenu();
        int count = Math.min(menu.size(), menuView.getChildCount());
        float d = nav.getResources().getDisplayMetrics().density;
        float liftPx = -6f * d;
        float elevationPx = 12f * d;
        /* Чуть крупнее выбранная иконка — визуально «жирнее» рядом с серыми */
        float scale = 1.14f;

        for (int i = 0; i < count; i++) {
            View child = menuView.getChildAt(i);
            boolean selected = menu.getItem(i).getItemId() == selectedItemId;
            child.animate()
                    .translationY(selected ? liftPx : 0f)
                    .scaleX(selected ? scale : 1f)
                    .scaleY(selected ? scale : 1f)
                    .setDuration(200)
                    .start();
            ViewCompat.setElevation(child, selected ? elevationPx : 0f);
        }
    }

    private static BottomNavigationMenuView findMenuView(@NonNull BottomNavigationView nav) {
        for (int i = 0; i < nav.getChildCount(); i++) {
            View v = nav.getChildAt(i);
            if (v instanceof BottomNavigationMenuView) {
                return (BottomNavigationMenuView) v;
            }
        }
        return null;
    }
}
