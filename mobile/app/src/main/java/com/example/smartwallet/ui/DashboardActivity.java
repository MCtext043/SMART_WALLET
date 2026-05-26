package com.example.smartwallet.ui;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartwallet.R;
import com.example.smartwallet.nfc.EmvNfcReader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DashboardActivity extends AppCompatActivity {

    private int currentTabId = R.id.tab_home;
    private boolean nfcReaderEnabled = false;
    private boolean nfcPromptShowing = false;
    private long lastNfcPromptAtMs = 0L;
    /** Демо-костыль: по очереди подставляем «ловушки» для 1-го и 2-го прикладывания карты по NFC. */
    private int nfcAddCardStubSequence = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setClipChildren(false);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            currentTabId = id;
            updateNfcReaderState();
            boolean handled;
            if (id == R.id.tab_home) {
                switchFragment(new HomeFragment());
                handled = true;
            } else if (id == R.id.tab_cards) {
                switchFragment(new CardsFragment());
                handled = true;
            } else if (id == R.id.tab_history) {
                switchFragment(new HistoryFragment());
                handled = true;
            } else if (id == R.id.tab_analytics) {
                switchFragment(new AnalyticsFragment());
                handled = true;
            } else if (id == R.id.tab_profile) {
                switchFragment(new ProfileFragment());
                handled = true;
            } else {
                handled = false;
            }
            if (handled) {
                bottomNav.post(() -> BottomNavSelectionHelper.applyLift(bottomNav, id));
            }
            return handled;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.tab_home);
        }
        bottomNav.post(() -> BottomNavSelectionHelper.applyLift(bottomNav, bottomNav.getSelectedItemId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNfcReaderState();
    }

    @Override
    protected void onPause() {
        disableNfcReader();
        super.onPause();
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void updateNfcReaderState() {
        if (currentTabId == R.id.tab_cards) {
            enableNfcReaderIfNeeded();
        } else {
            disableNfcReader();
        }
    }

    private void enableNfcReaderIfNeeded() {
        if (nfcReaderEnabled) return;
        nfcReaderEnabled = true;

        EmvNfcReader.start(this, new EmvNfcReader.Callback() {
            @Override
            public void onEmvCardDetected(@NonNull EmvNfcReader.EmvCardHint hint) {
                maybePromptAddCard(hint);
            }

            @Override
            public void onNfcNotAvailable() {
                // silent: device without NFC, user can still add manually
            }

            @Override
            public void onNfcDisabled() {
                // silent: NFC disabled, user can enable if they want
            }

            @Override
            public void onError(@NonNull String message) {
                // silent: avoid noisy toasts on every tap
            }
        });
    }

    private void disableNfcReader() {
        if (!nfcReaderEnabled) return;
        nfcReaderEnabled = false;
        EmvNfcReader.stop(this);
    }

    /** Вызвать после успешного POST /demo/seed — обновить текущую вкладку, если это карты или история. */
    public void notifyDemoSeeded() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (f instanceof CardsFragment) {
            ((CardsFragment) f).reloadFromServer();
        } else if (f instanceof HistoryFragment) {
            ((HistoryFragment) f).reloadFromServer();
        }
    }

    private void maybePromptAddCard(@NonNull EmvNfcReader.EmvCardHint hint) {
        long now = SystemClock.elapsedRealtime();
        if (nfcPromptShowing) return;
        if (now - lastNfcPromptAtMs < 2500) return; // debounce repeated taps
        lastNfcPromptAtMs = now;
        nfcPromptShowing = true;

        String scheme = hint.scheme != null ? hint.scheme : "EMV";
        String title = "Обнаружена карта (" + scheme + ")";
        String message = hint.panLast4 != null
                ? ("Хотите добавить новую карту?\n\nНомер: **** " + hint.panLast4)
                : "Хотите добавить новую карту?";

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_SmartWallet_MaterialAlertDialog_Rounded)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Нет", (d, which) -> d.dismiss())
                .setPositiveButton("Добавить", (d, which) -> {
                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (f instanceof CardsFragment) {
                        int idx = (nfcAddCardStubSequence++) % 2;
                        if (idx == 0) {
                            ((CardsFragment) f).showAddCardDialog(
                                    "Альфа Банк", "edition", "1055", "50200");
                        } else {
                            ((CardsFragment) f).showAddCardDialog(
                                    "Т-Банк", "gold card", "5032", "17000");
                        }
                    }
                })
                .setOnDismissListener(d -> nfcPromptShowing = false)
                .show();
    }
}





