package com.example.smartwallet.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.AssistantApi;
import com.example.smartwallet.network.TransactionsApi;
import com.example.smartwallet.network.dto.AnalyticsData;
import com.example.smartwallet.network.dto.ChatRequest;
import com.example.smartwallet.network.dto.ChatResponse;
import com.example.smartwallet.network.dto.Transaction;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.TokenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsViewModel extends AndroidViewModel {

    private final MutableLiveData<AnalyticsData> analyticsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> trendsInsight = new MutableLiveData<>();

    private final TransactionsApi transactionsApi;
    private final AssistantApi assistantApi;
    private TokenManager tokenManager;
    private int trendsRequestSeq;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        transactionsApi = ApiClient.getTransactionsApi();
        assistantApi = ApiClient.getAssistantApi();
        isLoading.setValue(false);
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public LiveData<AnalyticsData> getAnalyticsData() {
        return analyticsData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getTrendsInsight() {
        return trendsInsight;
    }

    /**
     * Повторный запрос к ассистенту при каждом показе экрана (если аналитика уже посчитана).
     */
    public void refreshAiTrends() {
        AnalyticsData data = analyticsData.getValue();
        if (data != null) {
            requestAiTrends(data);
        }
    }

    public void loadAnalytics() {
        String token = tokenManager.getToken();
        if (token == null) {
            error.setValue("Токен не найден. Войдите в систему.");
            return;
        }

        isLoading.setValue(true);
        String authToken = "Bearer " + token;

        transactionsApi.getTransactions(authToken).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AnalyticsData calculatedAnalytics = calculateAnalytics(response.body());
                    analyticsData.setValue(calculatedAnalytics);
                    requestAiTrends(calculatedAnalytics);
                } else {
                    error.setValue("Ошибка загрузки истории транзакций");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(ErrorHandler.getErrorMessage(t));
            }
        });
    }

    private void requestAiTrends(AnalyticsData data) {
        if (tokenManager == null) {
            trendsInsight.setValue(buildLocalTrendsText(data));
            return;
        }
        String token = tokenManager.getToken();
        if (token == null) {
            trendsInsight.setValue(buildLocalTrendsText(data));
            return;
        }

        final int seq = ++trendsRequestSeq;
        trendsInsight.setValue(getApplication().getString(R.string.analytics_trends_loading));

        String authToken = "Bearer " + token;
        ChatRequest request = new ChatRequest(buildTrendsPrompt(data));
        assistantApi.sendMessage(authToken, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (seq != trendsRequestSeq) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().reply;
                    if (reply != null && !reply.trim().isEmpty()) {
                        trendsInsight.setValue(reply.trim());
                        return;
                    }
                }
                trendsInsight.setValue(buildLocalTrendsText(data));
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                if (seq != trendsRequestSeq) {
                    return;
                }
                trendsInsight.setValue(buildLocalTrendsText(data));
            }
        });
    }

    private String buildTrendsPrompt(AnalyticsData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты финансовый ассистент приложения учёта трат. По данным ниже дай краткий вывод на русском языке: ")
                .append("2–4 предложения без заголовков и без маркированных списков. ")
                .append("Укажи, на что уходит основная доля бюджета, есть ли заметный перекос по категориям, ")
                .append("и один конкретный, дружелюбный совет. Не перечисляй все цифры подряд — обобщай.\n\n");

        sb.append("Кэшбэк (суммарно по операциям): ").append(Math.round(data.totalCashback)).append(" ₽\n");

        if (data.categorySpending != null && data.categorySpending.length > 0) {
            sb.append("Траты по категориям:\n");
            for (AnalyticsData.CategorySpending c : data.categorySpending) {
                if (c == null || c.amount <= 0) {
                    continue;
                }
                String name = c.category != null ? c.category : "Прочее";
                sb.append("- ").append(name).append(": ")
                        .append(Math.round(c.amount)).append(" ₽ (~")
                        .append(Math.round(c.percentage)).append("%)\n");
            }
        }

        if (data.spendingTrends != null && data.spendingTrends.trendPercentage > 0.0001) {
            sb.append("По категории «еда» (30 дней к предыдущим 30): на ")
                    .append(Math.round(data.spendingTrends.trendPercentage))
                    .append("% ")
                    .append(data.spendingTrends.foodTrend != null ? data.spendingTrends.foodTrend : "")
                    .append(", чем в предыдущем периоде.\n");
        }

        return sb.toString();
    }

    private String buildLocalTrendsText(AnalyticsData data) {
        Locale loc = Locale.getDefault();
        String intro = getApplication().getString(R.string.analytics_trends_intro);
        if (data.spendingTrends != null
                && data.spendingTrends.trendPercentage > 0.0001) {
            String insight = getApplication().getString(R.string.analytics_trends_insight_food,
                    data.spendingTrends.trendPercentage,
                    data.spendingTrends.foodTrend);
            return String.format(loc, "%s\n\n%s", intro, insight);
        }
        String example = getApplication().getString(R.string.analytics_trends_example);
        return String.format(loc, "%s\n\n%s", intro, example);
    }

    private AnalyticsData calculateAnalytics(List<Transaction> transactions) {
        AnalyticsData analytics = new AnalyticsData();

        double totalCashback = 0;
        for (Transaction transaction : transactions) {
            totalCashback += transaction.cashbackEarned;
        }
        analytics.totalCashback = totalCashback;

        Map<String, Double> categoryAmounts = new HashMap<>();
        double totalSpending = 0;

        for (Transaction transaction : transactions) {
            String category = transaction.category != null ? transaction.category : "Прочее";
            double amount = transaction.amount;

            categoryAmounts.put(category, categoryAmounts.getOrDefault(category, 0.0) + amount);
            totalSpending += amount;
        }

        List<AnalyticsData.CategorySpending> categorySpendingList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryAmounts.entrySet()) {
            AnalyticsData.CategorySpending categorySpending = new AnalyticsData.CategorySpending();
            categorySpending.category = entry.getKey();
            categorySpending.amount = entry.getValue();
            categorySpending.percentage = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
            categorySpendingList.add(categorySpending);
        }

        analytics.categorySpending = categorySpendingList.toArray(new AnalyticsData.CategorySpending[0]);
        analytics.spendingTrends = calculateTrends(transactions);

        return analytics;
    }

    private AnalyticsData.SpendingTrends calculateTrends(List<Transaction> transactions) {
        AnalyticsData.SpendingTrends trends = new AnalyticsData.SpendingTrends();

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date sixtyDaysAgo = calendar.getTime();

        double foodSpendingLast30Days = 0;
        double foodSpendingPrevious30Days = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

        for (Transaction transaction : transactions) {
            try {
                Date transactionDate = dateFormat.parse(transaction.createdAt);
                if (transaction.category != null && transaction.category.toLowerCase().contains("еда")) {
                    if (transactionDate.after(thirtyDaysAgo) && transactionDate.before(now)) {
                        foodSpendingLast30Days += transaction.amount;
                    } else if (transactionDate.after(sixtyDaysAgo) && transactionDate.before(thirtyDaysAgo)) {
                        foodSpendingPrevious30Days += transaction.amount;
                    }
                }
            } catch (ParseException e) {
                // ignore
            }
        }

        if (foodSpendingPrevious30Days > 0) {
            double percentageChange = ((foodSpendingLast30Days - foodSpendingPrevious30Days) / foodSpendingPrevious30Days) * 100;
            trends.trendPercentage = Math.abs(percentageChange);
            trends.foodTrend = percentageChange < 0 ? "меньше" : "больше";
        } else {
            trends.trendPercentage = 0;
            trends.foodTrend = "меньше";
        }

        return trends;
    }

    public void clearError() {
        error.setValue(null);
    }
}
