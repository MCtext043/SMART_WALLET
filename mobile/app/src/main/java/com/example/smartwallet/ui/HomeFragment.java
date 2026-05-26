package com.example.smartwallet.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.CardsApi;
import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.utils.AiFinanceTips;
import com.example.smartwallet.utils.CashbackRulesGenerator;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private MaterialCardView cardActiveCard;
    private MaterialCardView cardRecommendedCategory;
    private MaterialCardView cardRecommendationBottom;
    private TextView textActiveCardSectionTitle;
    private TextView textActiveCardNumber;
    private TextView textActiveCardName;
    private TextView textActiveCardBank;
    private TextView textActiveCardCashback;
    private TextView textRecommendedCategory;
    private TextView textRecommendation;
    private MaterialButton buttonSmartChoice;
    private MaterialButton buttonPay;
    private CircularProgressIndicator progress;

    private CardsApi cardsApi;
    private TokenManager tokenManager;

    private Card activeCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        applyHomeCardGlowShadow();
        setupClickListeners();
        
        cardsApi = ApiClient.getCardsApi();
        tokenManager = TokenManager.getInstance(requireContext());
        
        loadActiveCard();

        return view;
    }
    
    private void initViews(View view) {
        cardActiveCard = view.findViewById(R.id.cardActiveCard);
        cardRecommendedCategory = view.findViewById(R.id.cardRecommendedCategory);
        cardRecommendationBottom = view.findViewById(R.id.cardRecommendationBottom);
        textActiveCardSectionTitle = view.findViewById(R.id.textActiveCardSectionTitle);
        textActiveCardNumber = view.findViewById(R.id.textActiveCardNumber);
        textActiveCardName = view.findViewById(R.id.textActiveCardName);
        textActiveCardBank = view.findViewById(R.id.textActiveCardBank);
        textActiveCardCashback = view.findViewById(R.id.textActiveCardCashback);
        textRecommendedCategory = view.findViewById(R.id.textRecommendedCategory);
        textRecommendation = view.findViewById(R.id.textRecommendation);
        buttonSmartChoice = view.findViewById(R.id.buttonSmartChoice);
        buttonPay = view.findViewById(R.id.buttonPay);
        progress = view.findViewById(R.id.progress);
    }

    /** Свечение без обводки (цвет контура тени — API 28+). У «Умного выбора» — как у блока рекомендованной категории. */
    private void applyHomeCardGlowShadow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        int glow = ContextCompat.getColor(requireContext(), R.color.home_card_glow_shadow);
        MaterialCardView[] cards = {
                cardActiveCard,
                cardRecommendedCategory,
                cardRecommendationBottom
        };
        for (MaterialCardView card : cards) {
            if (card == null) continue;
            card.setOutlineAmbientShadowColor(glow);
            card.setOutlineSpotShadowColor(glow);
        }
        buttonSmartChoice.setOutlineAmbientShadowColor(glow);
        buttonSmartChoice.setOutlineSpotShadowColor(glow);
    }

    private void setupClickListeners() {
        buttonSmartChoice.setOnClickListener(v -> applyRandomAiTip());
        buttonPay.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PayQrScanActivity.class)));
    }
    
    private void loadActiveCard() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Токен не найден. Войдите в систему.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        String authToken = "Bearer " + token;
        
        cardsApi.getCards(authToken).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    activeCard = response.body().get(0); // Take first card as active
                    updateActiveCardDisplay();
                } else {
                    Toast.makeText(requireContext(), "Нет доступных карт", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                setLoading(false);
                ErrorHandler.showError(requireContext(), t);
            }
        });
    }
    
    /** Новый короткий совет — только по нажатию «Финансовый совет». */
    private void applyRandomAiTip() {
        if (textRecommendation == null) return;
        textRecommendation.setText(AiFinanceTips.pickRandom(requireContext()));
    }
    
    private void updateActiveCardDisplay() {
        if (activeCard == null || cardActiveCard == null) return;

        BankCardStyle style = BankCardStyle.forCard(activeCard);
        style.apply(cardActiveCard);

        int primary = style.primaryTextColor(requireContext());
        int secondary = style.secondaryTextColor(requireContext());
        textActiveCardSectionTitle.setTextColor(secondary);
        textActiveCardNumber.setTextColor(primary);
        textActiveCardName.setTextColor(primary);
        textActiveCardBank.setTextColor(secondary);
        textActiveCardBank.setAlpha(1f);
        textActiveCardCashback.setTextColor(primary);

        textActiveCardNumber.setText("•••• " + activeCard.last4);
        textActiveCardName.setText(activeCard.cardName);
        textActiveCardBank.setText(activeCard.bankName);

        if (activeCard.cashbackRules != null && !activeCard.cashbackRules.isEmpty()) {
            double avgCashback = CashbackRulesGenerator.averagePercent(activeCard.cashbackRules);
            textActiveCardCashback.setText(String.format("%.0f%% кэшбэк", avgCashback));
        } else {
            textActiveCardCashback.setText("—");
        }
    }
    
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSmartChoice.setEnabled(!loading);
        buttonPay.setEnabled(!loading);
    }
}





