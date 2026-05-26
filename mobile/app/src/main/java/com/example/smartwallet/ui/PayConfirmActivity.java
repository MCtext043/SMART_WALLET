package com.example.smartwallet.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.CardsApi;
import com.example.smartwallet.network.CashbackApi;
import com.example.smartwallet.network.TransactionsApi;
import com.example.smartwallet.network.dto.BestCardResponse;
import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.network.dto.TransactionRequest;
import com.example.smartwallet.utils.CardCashbackEstimator;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.PayQrPayloadHelper;
import com.example.smartwallet.utils.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Экран после сканирования QR: получатель, категория, карты по кэшбэку, выбор карты, подтверждение.
 */
public final class PayConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_QR_RAW = "qr_raw";

    private TextView textPayMerchantName;
    private TextView textPayCategory;
    private TextView textPayAmount;
    private TextView textPayTime;
    private TextView textBestCardRecommendation;
    private TextView textPayCardsListHeader;
    private RadioGroup radioGroupPayCards;
    private CircularProgressIndicator progressBestCard;
    private MaterialButton buttonConfirmPurchase;
    private LinearProgressIndicator progressConfirmPay;

    private final SparseIntArray radioButtonIdToCardId = new SparseIntArray();

    @Nullable
    private String qrRaw;
    @NonNull
    private String payCategory = "прочее";
    private int selectedCardId = -1;
    private boolean payInProgress;
    private double payAmountRub = 1.0;

    private final List<Card> pendingCards = new ArrayList<>();
    @Nullable
    private BestCardResponse pendingBest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_confirm);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPayConfirm);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        textPayMerchantName = findViewById(R.id.textPayMerchantName);
        textPayCategory = findViewById(R.id.textPayCategory);
        textPayAmount = findViewById(R.id.textPayAmount);
        textPayTime = findViewById(R.id.textPayTime);
        textBestCardRecommendation = findViewById(R.id.textBestCardRecommendation);
        textPayCardsListHeader = findViewById(R.id.textPayCardsListHeader);
        radioGroupPayCards = findViewById(R.id.radioGroupPayCards);
        progressBestCard = findViewById(R.id.progressBestCard);
        buttonConfirmPurchase = findViewById(R.id.buttonConfirmPurchase);
        progressConfirmPay = findViewById(R.id.progressConfirmPay);

        String raw = getIntent().getStringExtra(EXTRA_QR_RAW);
        if (raw == null || raw.isEmpty()) {
            Toast.makeText(this, R.string.pay_confirm_no_qr, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        qrRaw = raw;
        payCategory = PayQrPayloadHelper.detectCategory(raw);

        bindPaymentHeader(raw);

        buttonConfirmPurchase.setOnClickListener(v -> confirmPurchase());

        radioGroupPayCards.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                selectedCardId = radioButtonIdToCardId.get(checkedId, -1);
                updateConfirmButtonState();
            }
        });

        loadCardsAndServerBest(raw);
    }

    private void bindPaymentHeader(@NonNull String raw) {
        Locale ru = Locale.forLanguageTag("ru");
        String merchant = PayQrPayloadHelper.parseMerchantName(raw);
        textPayMerchantName.setText(merchant != null && !merchant.isEmpty()
                ? merchant
                : getString(R.string.pay_confirm_payee_unknown));
        textPayCategory.setText(getString(
                R.string.pay_confirm_category_line,
                PayQrPayloadHelper.categoryDisplayTitle(payCategory)));

        Double amountParsed = PayQrPayloadHelper.parseAmountRub(raw);
        if (amountParsed != null) {
            payAmountRub = amountParsed;
            textPayAmount.setText(String.format(ru, "%.2f ₽", amountParsed));
        } else {
            payAmountRub = 1.0;
            textPayAmount.setText(getString(R.string.pay_confirm_amount_not_in_qr));
        }

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, ru);
        textPayTime.setText(getString(R.string.pay_confirm_time_template, df.format(new Date())));
    }

    private void updateConfirmButtonState() {
        if (buttonConfirmPurchase == null) {
            return;
        }
        String token = TokenManager.getInstance(this).getToken();
        boolean ok = token != null && selectedCardId > 0 && !payInProgress;
        buttonConfirmPurchase.setEnabled(ok);
    }

    private void confirmPurchase() {
        if (payInProgress || qrRaw == null) {
            return;
        }
        if (selectedCardId <= 0) {
            Toast.makeText(this, R.string.pay_confirm_need_card, Toast.LENGTH_SHORT).show();
            return;
        }
        String token = TokenManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, R.string.pay_confirm_need_login, Toast.LENGTH_SHORT).show();
            return;
        }
        Double parsedAmount = PayQrPayloadHelper.parseAmountRub(qrRaw);
        double amount = parsedAmount != null ? parsedAmount : 1.0;
        if (parsedAmount == null) {
            Toast.makeText(this, R.string.pay_confirm_amount_default_hint, Toast.LENGTH_LONG).show();
        }

        payInProgress = true;
        progressConfirmPay.setVisibility(View.VISIBLE);
        updateConfirmButtonState();

        TransactionRequest request = new TransactionRequest(amount, payCategory, selectedCardId);
        TransactionsApi api = ApiClient.getTransactionsApi();
        api.createTransaction("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                payInProgress = false;
                progressConfirmPay.setVisibility(View.GONE);
                updateConfirmButtonState();
                if (response.isSuccessful()) {
                    Toast.makeText(PayConfirmActivity.this, R.string.pay_confirm_success, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (response.code() == 401) {
                    TokenManager.getInstance(PayConfirmActivity.this).clearToken();
                    Toast.makeText(PayConfirmActivity.this, "Сессия истекла. Войдите снова.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Toast.makeText(PayConfirmActivity.this, R.string.pay_confirm_pay_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                payInProgress = false;
                progressConfirmPay.setVisibility(View.GONE);
                updateConfirmButtonState();
                Toast.makeText(PayConfirmActivity.this, R.string.pay_confirm_pay_error, Toast.LENGTH_SHORT).show();
                ErrorHandler.showError(PayConfirmActivity.this, t);
            }
        });
    }

    private void loadCardsAndServerBest(@NonNull String raw) {
        String token = TokenManager.getInstance(this).getToken();
        if (token == null) {
            progressBestCard.setVisibility(View.GONE);
            textBestCardRecommendation.setText(R.string.pay_confirm_need_login);
            textPayCardsListHeader.setVisibility(View.GONE);
            radioGroupPayCards.setVisibility(View.GONE);
            selectedCardId = -1;
            updateConfirmButtonState();
            return;
        }

        payCategory = PayQrPayloadHelper.detectCategory(raw);
        progressBestCard.setVisibility(View.VISIBLE);
        textBestCardRecommendation.setText("");
        textPayCardsListHeader.setVisibility(View.VISIBLE);
        radioGroupPayCards.setVisibility(View.VISIBLE);
        radioGroupPayCards.removeAllViews();
        radioButtonIdToCardId.clear();
        radioGroupPayCards.clearCheck();
        selectedCardId = -1;
        pendingCards.clear();
        pendingBest = null;

        String auth = "Bearer " + token;
        String category = payCategory;
        AtomicInteger pending = new AtomicInteger(2);

        CardsApi cardsApi = ApiClient.getCardsApi();
        CashbackApi cashbackApi = ApiClient.getCashbackApi();

        Runnable finishLoading = () -> {
            if (pending.decrementAndGet() != 0) {
                return;
            }
            applyLoadedCardState();
        };

        cardsApi.getCards(auth).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                pendingCards.clear();
                if (response.isSuccessful() && response.body() != null) {
                    pendingCards.addAll(response.body());
                }
                finishLoading.run();
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                pendingCards.clear();
                finishLoading.run();
            }
        });

        cashbackApi.getBestCard(auth, category).enqueue(new Callback<BestCardResponse>() {
            @Override
            public void onResponse(@NonNull Call<BestCardResponse> call, @NonNull Response<BestCardResponse> response) {
                pendingBest = response.isSuccessful() ? response.body() : null;
                finishLoading.run();
            }

            @Override
            public void onFailure(@NonNull Call<BestCardResponse> call, @NonNull Throwable t) {
                pendingBest = null;
                finishLoading.run();
            }
        });
    }

    private void applyLoadedCardState() {
        progressBestCard.setVisibility(View.GONE);

        Locale ru = Locale.forLanguageTag("ru");
        List<Card> sorted = new ArrayList<>(pendingCards);
        Comparator<Card> byCashbackDesc = (a, b) -> {
            double pa = CardCashbackEstimator.cashbackRubForAmount(a, payCategory, payAmountRub);
            double pb = CardCashbackEstimator.cashbackRubForAmount(b, payCategory, payAmountRub);
            int c = Double.compare(pb, pa);
            if (c != 0) {
                return c;
            }
            int pcmp = Integer.compare(
                    CardCashbackEstimator.percentForCategory(b, payCategory),
                    CardCashbackEstimator.percentForCategory(a, payCategory));
            if (pcmp != 0) {
                return pcmp;
            }
            return Integer.compare(a.id, b.id);
        };
        Collections.sort(sorted, byCashbackDesc);

        if (sorted.isEmpty()) {
            textBestCardRecommendation.setText(R.string.pay_confirm_no_cards_saved);
            selectedCardId = -1;
            updateConfirmButtonState();
            return;
        }

        BestCardResponse best = pendingBest;
        if (best != null && best.cardId > 0) {
            String bank = best.bankName != null ? best.bankName : "";
            String cardN = best.cardName != null ? best.cardName : "";
            String line = String.format(ru, "%s %s — %d%% кэшбэк (категория: %s)",
                    bank.trim(),
                    cardN.trim(),
                    best.cashbackPercentage,
                    PayQrPayloadHelper.categoryDisplayTitle(payCategory));
            textBestCardRecommendation.setText(line.trim());
        } else {
            Card top = sorted.get(0);
            int pct = CardCashbackEstimator.percentForCategory(top, payCategory);
            double rub = CardCashbackEstimator.cashbackRubForAmount(top, payCategory, payAmountRub);
            String bank = top.bankName != null ? top.bankName : "";
            String cardN = top.cardName != null ? top.cardName : "";
            String line = String.format(ru, "%s %s — %d%% кэшбэк · +%.2f ₽ на эту покупку",
                    bank.trim(),
                    cardN.trim(),
                    pct,
                    rub);
            textBestCardRecommendation.setText(line.trim());
        }

        int preferredId = -1;
        if (best != null && best.cardId > 0) {
            for (Card c : sorted) {
                if (c.id == best.cardId) {
                    preferredId = c.id;
                    break;
                }
            }
        }
        if (preferredId <= 0) {
            preferredId = sorted.get(0).id;
        }
        selectedCardId = preferredId;

        radioGroupPayCards.removeAllViews();
        radioButtonIdToCardId.clear();
        for (Card c : sorted) {
            MaterialRadioButton rb = new MaterialRadioButton(this);
            int rid = View.generateViewId();
            rb.setId(rid);
            rb.setText(formatCardRadioLabel(c));
            radioButtonIdToCardId.put(rid, c.id);
            radioGroupPayCards.addView(rb);
        }

        for (int i = 0; i < radioGroupPayCards.getChildCount(); i++) {
            View child = radioGroupPayCards.getChildAt(i);
            if (radioButtonIdToCardId.get(child.getId(), -1) == selectedCardId) {
                radioGroupPayCards.check(child.getId());
                break;
            }
        }

        updateConfirmButtonState();
    }

    @NonNull
    private String formatCardRadioLabel(@NonNull Card c) {
        String nameLine;
        if (!TextUtils.isEmpty(c.bankName) && !TextUtils.isEmpty(c.cardName)) {
            nameLine = c.bankName.trim() + " · " + c.cardName.trim();
        } else if (!TextUtils.isEmpty(c.cardName)) {
            nameLine = c.cardName.trim();
        } else if (!TextUtils.isEmpty(c.bankName)) {
            nameLine = c.bankName.trim();
        } else {
            nameLine = "Карта";
        }
        String last = c.last4 != null ? c.last4 : "—";
        int pct = CardCashbackEstimator.percentForCategory(c, payCategory);
        double rub = CardCashbackEstimator.cashbackRubForAmount(c, payCategory, payAmountRub);
        return getString(R.string.pay_confirm_card_option_format, nameLine, last, pct, rub);
    }
}
