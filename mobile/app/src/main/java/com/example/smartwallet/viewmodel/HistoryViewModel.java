package com.example.smartwallet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.CardsApi;
import com.example.smartwallet.network.TransactionsApi;
import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.network.dto.Transaction;
import com.example.smartwallet.utils.CardDisplayFormatter;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.Logger;
import com.example.smartwallet.utils.NetworkUtils;
import com.example.smartwallet.utils.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryViewModel extends ViewModel {
    
    private MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private MutableLiveData<Map<Integer, String>> cardLabels = new MutableLiveData<>(Collections.emptyMap());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    
    private TransactionsApi transactionsApi;
    private CardsApi cardsApi;
    private TokenManager tokenManager;
    private List<Transaction> transactionList = new ArrayList<>();
    
    public HistoryViewModel() {
        transactionsApi = ApiClient.getTransactionsApi();
        cardsApi = ApiClient.getCardsApi();
        transactions.setValue(transactionList);
        isLoading.setValue(false);
    }
    
    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }
    
    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    /** id карты → «Банк · Название» для строк истории. */
    public LiveData<Map<Integer, String>> getCardLabels() {
        return cardLabels;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public void loadTransactions() {
        String token = tokenManager.getToken();
        if (token == null) {
            Logger.w("HistoryViewModel", "Token not found");
            error.setValue("Токен не найден. Войдите в систему.");
            return;
        }
        
        // Check network connectivity (optional - will not block if permission is missing)
        try {
            if (!NetworkUtils.isNetworkAvailable(tokenManager.getContext())) {
                Logger.w("HistoryViewModel", "No network connection");
                error.setValue("Нет подключения к интернету. Проверьте соединение и попробуйте снова.");
                return;
            }
        } catch (Exception e) {
            Logger.w("HistoryViewModel", "Could not check network state, proceeding anyway");
            // Continue with the request even if we can't check network state
        }
        
        Logger.d("HistoryViewModel", "Loading transactions");
        isLoading.setValue(true);
        String authToken = "Bearer " + token;
        
        transactionsApi.getTransactions(authToken).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Logger.i("HistoryViewModel", "Transactions loaded successfully: " + response.body().size() + " items");
                    transactionList = response.body();
                    transactions.setValue(new ArrayList<>(transactionList));
                    loadCardLabelsForHistory();
                } else {
                    String errorMsg = "Ошибка загрузки истории транзакций. Код: " + response.code();
                    Logger.e("HistoryViewModel", errorMsg);
                    error.setValue(errorMsg);
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Logger.e("HistoryViewModel", "Failed to load transactions", t);
                error.setValue(ErrorHandler.getErrorMessage(t));
                isLoading.setValue(false);
            }
        });
    }
    
    public void clearError() {
        error.setValue(null);
    }

    private void loadCardLabelsForHistory() {
        String token = tokenManager.getToken();
        if (token == null) {
            return;
        }
        String auth = "Bearer " + token;
        cardsApi.getCards(auth).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(Call<List<Card>> call, Response<List<Card>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                Map<Integer, String> map = new HashMap<>();
                for (Card c : response.body()) {
                    if (c != null && c.id > 0) {
                        map.put(c.id, CardDisplayFormatter.bankAndCardName(c));
                    }
                }
                cardLabels.setValue(map);
            }

            @Override
            public void onFailure(Call<List<Card>> call, Throwable t) {
                Logger.w("HistoryViewModel", "Cards for history labels failed: " + t.getMessage());
            }
        });
    }
}
