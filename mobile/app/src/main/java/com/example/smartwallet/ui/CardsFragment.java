package com.example.smartwallet.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.CardsApi;
import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.network.dto.CardRequest;
import com.example.smartwallet.ui.adapter.CardsAdapter;
import com.example.smartwallet.utils.CashbackRulesGenerator;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardsFragment extends Fragment implements CardsAdapter.OnCardClickListener {

    private RecyclerView recyclerCards;
    private LinearLayout emptyState;
    private View errorState;
    private View progress;
    private View rowSelectedCashback;
    private FloatingActionButton fabAddCard;
    private MaterialCardView cardSelectedCardInfo;
    private MaterialCardView cardCardsStatistics;
    private TextView textSelectedCardTitle;
    private TextView textSelectedCardDetails;
    private TextView textSelectedCardCategories;
    private CardsAdapter cardsAdapter;
    private CardsApi cardsApi;
    private TokenManager tokenManager;
    private final PagerSnapHelper snapHelper = new PagerSnapHelper();
    
    // Statistics views
    private TextView textTotalCards;
    private TextView textTotalLimit;
    private TextView textAvgCashback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards, container, false);
        
        initViews(view);
        applyCardsScreenGlow();
        setupRecyclerView();
        setupClickListeners();
        
        cardsApi = ApiClient.getCardsApi();
        tokenManager = TokenManager.getInstance(requireContext());
        
        loadCards();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerCards = view.findViewById(R.id.recyclerCards);
        emptyState = view.findViewById(R.id.emptyState);
        errorState = view.findViewById(R.id.errorState);
        progress = view.findViewById(R.id.progress);
        fabAddCard = view.findViewById(R.id.fabAddCard);
        cardSelectedCardInfo = view.findViewById(R.id.cardSelectedCardInfo);
        cardCardsStatistics = view.findViewById(R.id.cardCardsStatistics);
        textSelectedCardTitle = view.findViewById(R.id.textSelectedCardTitle);
        textSelectedCardDetails = view.findViewById(R.id.textSelectedCardDetails);
        textSelectedCardCategories = view.findViewById(R.id.textSelectedCardCategories);
        rowSelectedCashback = view.findViewById(R.id.rowSelectedCashback);

        // Statistics views
        textTotalCards = view.findViewById(R.id.textTotalCards);
        textTotalLimit = view.findViewById(R.id.textTotalLimit);
        textAvgCashback = view.findViewById(R.id.textAvgCashback);
        
        fabAddCard.setOnClickListener(v -> showAddCardDialog());
        
        // Setup error state retry button
        setupErrorState();
    }

    /**
     * Центрирует карточку 288dp + отступы и оставляет по краям «зазор», чтобы соседняя карта
     * слегка выглядывала (clipToPadding=false).
     */
    private void applyCarouselHorizontalPadding() {
        if (recyclerCards == null) {
            return;
        }
        int w = recyclerCards.getWidth();
        if (w <= 0) {
            return;
        }
        Resources res = getResources();
        float d = res.getDisplayMetrics().density;
        int cardTotalPx = Math.round((288f + 8f + 8f) * d);
        int pad = Math.max(0, (w - cardTotalPx) / 2);
        int vertical = Math.round(8f * d);
        recyclerCards.setPadding(pad, vertical, pad, vertical);
    }

    private void applyCardsScreenGlow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        int glow = ContextCompat.getColor(requireContext(), R.color.home_card_glow_shadow);
        if (cardSelectedCardInfo != null) {
            cardSelectedCardInfo.setOutlineAmbientShadowColor(glow);
            cardSelectedCardInfo.setOutlineSpotShadowColor(glow);
        }
        if (cardCardsStatistics != null) {
            cardCardsStatistics.setOutlineAmbientShadowColor(glow);
            cardCardsStatistics.setOutlineSpotShadowColor(glow);
        }
    }
    
    private void setupRecyclerView() {
        cardsAdapter = new CardsAdapter();
        cardsAdapter.setOnCardClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), 
                LinearLayoutManager.HORIZONTAL, false);
        recyclerCards.setLayoutManager(layoutManager);
        recyclerCards.setAdapter(cardsAdapter);

        recyclerCards.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (r - l != or - ol) {
                applyCarouselHorizontalPadding();
            }
        });
        recyclerCards.post(this::applyCarouselHorizontalPadding);
        
        snapHelper.attachToRecyclerView(recyclerCards);
        recyclerCards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateSelectedCardInfo();
                }
            }
        });
    }
    
    private void setupClickListeners() {
        fabAddCard.setOnClickListener(v -> showAddCardDialog());
    }
    
    private void setupErrorState() {
        if (errorState != null) {
            Button retryButton = errorState.findViewById(R.id.buttonRetry);
            if (retryButton != null) {
                retryButton.setOnClickListener(v -> {
                    hideErrorState();
                    loadCards();
                });
            }
        }
    }
    
    private void showErrorState(String message) {
        if (errorState != null) {
            TextView errorMessage = errorState.findViewById(R.id.textErrorMessage);
            TextView errorDescription = errorState.findViewById(R.id.textErrorDescription);
            
            if (errorMessage != null) {
                errorMessage.setText("Ошибка загрузки карт");
            }
            if (errorDescription != null) {
                errorDescription.setText(message);
            }
            
            errorState.setVisibility(View.VISIBLE);
            recyclerCards.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    private void hideErrorState() {
        if (errorState != null) {
            errorState.setVisibility(View.GONE);
        }
    }
    
    /** После демо-сидинга: обновить список, если вкладка «Карты» на экране. */
    public void reloadFromServer() {
        loadCards();
    }

    private void loadCards() {
        String token = tokenManager.getToken();
        if (token == null) {
            showErrorState("Токен не найден. Войдите в систему.");
            return;
        }
        
        hideErrorState();
        setLoading(true);
        String authToken = "Bearer " + token;
        
        cardsApi.getCards(authToken).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Card> cards = response.body();
                    cardsAdapter.setCards(cards);
                    updateEmptyState(cards.isEmpty());
                    updateStatistics(cards);
                } else if (response.code() == 401) {
                    showErrorState("Сессия истекла. Войдите в систему.");
                    tokenManager.clearToken();
                } else {
                    showErrorState("Ошибка загрузки карт. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                setLoading(false);
                showErrorState(ErrorHandler.getErrorMessage(t));
            }
        });
    }
    
    private void showAddCardDialog() {
        showAddCardDialog(null, null, null, null);
    }

    /**
     * @param bankName,cardName,last4,limitMonthly опциональное предзаполнение (например после NFC).
     */
    public void showAddCardDialog(
            @Nullable String bankName,
            @Nullable String cardName,
            @Nullable String last4,
            @Nullable String limitMonthly) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_card);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        EditText editBankName = dialog.findViewById(R.id.editBankName);
        EditText editCardName = dialog.findViewById(R.id.editCardName);
        EditText editLast4 = dialog.findViewById(R.id.editLast4);
        EditText editLimitMonthly = dialog.findViewById(R.id.editLimitMonthly);

        if (!TextUtils.isEmpty(bankName) && editBankName != null) {
            editBankName.setText(bankName);
        }
        if (!TextUtils.isEmpty(cardName) && editCardName != null) {
            editCardName.setText(cardName);
        }
        if (!TextUtils.isEmpty(last4) && editLast4 != null) {
            editLast4.setText(last4);
        }
        if (!TextUtils.isEmpty(limitMonthly) && editLimitMonthly != null) {
            editLimitMonthly.setText(limitMonthly);
        }
        
        dialog.findViewById(R.id.buttonCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.buttonSave).setOnClickListener(v -> {
            if (validateCardInput(editBankName, editCardName, editLast4, editLimitMonthly)) {
                saveCard(editBankName.getText().toString().trim(),
                        editCardName.getText().toString().trim(),
                        editLast4.getText().toString().trim(),
                        editLimitMonthly.getText().toString().trim(),
                        dialog);
            }
        });
        
        dialog.show();
    }
    
    private boolean validateCardInput(EditText bankName, EditText cardName, EditText last4, EditText limit) {
        if (TextUtils.isEmpty(bankName.getText())) {
            bankName.setError("Введите название банка");
            return false;
        }
        if (TextUtils.isEmpty(cardName.getText())) {
            cardName.setError("Введите название карты");
            return false;
        }
        if (TextUtils.isEmpty(last4.getText()) || last4.getText().length() != 4) {
            last4.setError("Введите 4 последние цифры");
            return false;
        }
        if (TextUtils.isEmpty(limit.getText())) {
            limit.setError("Введите месячный лимит");
            return false;
        }
        return true;
    }
    
    private void saveCard(String bankName, String cardName, String last4, String limitStr, Dialog dialog) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Токен не найден. Войдите в систему.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double limit = Double.parseDouble(limitStr);
            CardRequest request = new CardRequest(
                    bankName,
                    cardName,
                    last4,
                    CashbackRulesGenerator.generate(new Random()),
                    limit);
            
            String authToken = "Bearer " + token;
            cardsApi.createCard(authToken, request).enqueue(new Callback<Card>() {
                @Override
                public void onResponse(@NonNull Call<Card> call, @NonNull Response<Card> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Карта добавлена успешно", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCards(); // Refresh the list
                } else {
                    String errorMessage = "Ошибка добавления карты";
                    if (response.code() == 401) {
                        errorMessage = "Сессия истекла. Войдите в систему.";
                        tokenManager.clearToken();
                    } else if (response.code() == 422) {
                        errorMessage = "Ошибка валидации данных. Проверьте правильность заполнения полей";
                    } else if (response.code() == 400) {
                        errorMessage = "Неверные данные карты";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
                }

                @Override
                public void onFailure(@NonNull Call<Card> call, @NonNull Throwable t) {
                    ErrorHandler.showError(requireContext(), t);
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Неверный формат лимита", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerCards.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
    
    private void updateEmptyState(boolean isEmpty) {
        hideErrorState();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerCards.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (cardSelectedCardInfo != null) {
            cardSelectedCardInfo.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (!isEmpty) {
            recyclerCards.post(() -> updateSelectedCardInfo());
        }
    }

    private void updateSelectedCardInfo() {
        if (textSelectedCardTitle == null || textSelectedCardDetails == null || cardsAdapter == null) return;
        if (cardsAdapter.getCardCount() == 0) return;
        LinearLayoutManager lm = (LinearLayoutManager) recyclerCards.getLayoutManager();
        if (lm == null) return;
        View snap = snapHelper.findSnapView(lm);
        int pos = snap != null ? lm.getPosition(snap) : 0;
        if (pos < 0) pos = 0;
        Card c = cardsAdapter.getCardAt(pos);
        if (c == null) return;
        String title = !TextUtils.isEmpty(c.cardName) ? c.cardName : (c.bankName != null ? c.bankName : "Карта");
        textSelectedCardTitle.setText(title);
        textSelectedCardDetails.setText("•••• " + (c.last4 != null ? c.last4 : "—"));
        bindSelectedCardCashbackCategories(c);
    }

    private void bindSelectedCardCashbackCategories(@NonNull Card c) {
        if (textSelectedCardCategories == null) return;
        if (c.cashbackRules == null || c.cashbackRules.isEmpty()) {
            textSelectedCardCategories.setText("");
            if (rowSelectedCashback != null) {
                rowSelectedCashback.setVisibility(View.GONE);
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : c.cashbackRules.entrySet()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(e.getKey()).append(" — ").append(e.getValue()).append('%');
        }
        textSelectedCardCategories.setText(sb.toString());
        if (rowSelectedCashback != null) {
            rowSelectedCashback.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    private String formatCardSummaryMessage(@NonNull Card c) {
        String name = !TextUtils.isEmpty(c.cardName) ? c.cardName : (c.bankName != null ? c.bankName : "Карта");
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n•••• ").append(c.last4 != null ? c.last4 : "—");
        if (c.cashbackRules != null && !c.cashbackRules.isEmpty()) {
            sb.append("\n\nКэшбэк по категориям:");
            for (Map.Entry<String, Integer> e : c.cashbackRules.entrySet()) {
                sb.append("\n").append(e.getKey()).append(": ").append(e.getValue()).append("%");
            }
        }
        return sb.toString();
    }

    private void showCardDetailsDialog(@NonNull Card card) {
        String dialogTitle = !TextUtils.isEmpty(card.bankName) ? card.bankName : "Карта";
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_SmartWallet_MaterialAlertDialog_Rounded)
                .setTitle(dialogTitle)
                .setMessage(formatCardSummaryMessage(card))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    
    @Override
    public void onCardClick(Card card) {
        showCardDetailsDialog(card);
    }
    
    private void updateStatistics(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            textTotalCards.setText("0");
            textTotalLimit.setText("0 ₽");
            textAvgCashback.setText("0%");
            return;
        }
        
        // Total cards count
        textTotalCards.setText(String.valueOf(cards.size()));
        
        // Total limit
        double totalLimit = 0;
        double totalCashback = 0;
        
        for (Card card : cards) {
            totalLimit += card.limitMonthly;
            
            totalCashback += CashbackRulesGenerator.averagePercent(card.cashbackRules);
        }
        
        textTotalLimit.setText(String.format("%.0f ₽", totalLimit));
        
        // Average cashback
        double avgCashback = cards.size() > 0 ? totalCashback / cards.size() : 0;
        textAvgCashback.setText(String.format("%.1f%%", avgCashback));
    }
}




