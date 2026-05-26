package com.example.smartwallet.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartwallet.R;
import com.example.smartwallet.network.dto.Transaction;
import com.example.smartwallet.ui.adapter.TransactionsAdapter;
import com.example.smartwallet.utils.DateUtils;
import com.example.smartwallet.utils.TokenManager;
import com.example.smartwallet.viewmodel.HistoryViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {

    /** Первый onResume совпадает с начальной загрузкой во viewModel — не дублируем запрос. */
    private int historyResumeCount;

    private RecyclerView recyclerHistory;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private ProgressBar progress;
    private TransactionsAdapter transactionsAdapter;
    private HistoryViewModel historyViewModel;
    private Map<Integer, String> cardLabelsForDetails = Collections.emptyMap();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupViewModel();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        historyResumeCount++;
        if (historyResumeCount > 1 && historyViewModel != null) {
            historyViewModel.loadTransactions();
        }
    }
    
    private void initViews(View view) {
        recyclerHistory = view.findViewById(R.id.recyclerHistory);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        emptyState = view.findViewById(R.id.emptyState);
        progress = view.findViewById(R.id.progress);
        
        // Настройка SwipeRefreshLayout
        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            historyViewModel.loadTransactions();
        });
    }
    
    private void setupRecyclerView() {
        transactionsAdapter = new TransactionsAdapter();
        transactionsAdapter.setOnTransactionClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerHistory.setLayoutManager(layoutManager);
        recyclerHistory.setAdapter(transactionsAdapter);
    }
    
    private void setupViewModel() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        historyViewModel.setTokenManager(TokenManager.getInstance(requireContext()));
        
        // Observe transactions
        historyViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            transactionsAdapter.setTransactions(transactions);
            updateEmptyState(transactions.isEmpty());
        });

        historyViewModel.getCardLabels().observe(getViewLifecycleOwner(), labels -> {
            cardLabelsForDetails = labels != null ? labels : Collections.emptyMap();
            transactionsAdapter.setCardLabels(cardLabelsForDetails);
        });
        
        // Observe loading state
        historyViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            setLoading(isLoading);
            swipeRefresh.setRefreshing(isLoading);
        });
        
        // Observe errors
        historyViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                historyViewModel.clearError();
            }
        });
        
        // Load transactions
        historyViewModel.loadTransactions();
    }
    
    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        // Иначе половина экрана уходит под пустой SwipeRefresh — «пуста» оказывается слишком низко
        swipeRefresh.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            recyclerHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.VISIBLE);
        } else {
            List<Transaction> t = historyViewModel.getTransactions().getValue();
            updateEmptyState(t == null || t.isEmpty());
        }
    }
    
    @Override
    public void onTransactionClick(Transaction transaction) {
        showTransactionDetailsDialog(transaction);
    }
    
    private void showTransactionDetailsDialog(Transaction transaction) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_transaction_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        // Найти элементы диалога
        TextView textAmount = dialog.findViewById(R.id.textAmount);
        TextView textCategory = dialog.findViewById(R.id.textCategory);
        TextView textDate = dialog.findViewById(R.id.textDate);
        TextView textCard = dialog.findViewById(R.id.textCard);
        TextView textMcc = dialog.findViewById(R.id.textMcc);
        TextView textCashback = dialog.findViewById(R.id.textCashback);
        TextView textSource = dialog.findViewById(R.id.textTransactionSource);
        View rowSource = dialog.findViewById(R.id.rowTransactionSource);
        
        // Заполнить данные
        textAmount.setText(String.format(Locale.US, "-%.2f ₽", Math.abs(transaction.amount)));
        textCategory.setText(transaction.category);
        textDate.setText(DateUtils.formatTransactionDisplayDate(transaction));
        String cardLine = cardLabelsForDetails.get(transaction.cardId);
        textCard.setText(cardLine != null && !cardLine.isEmpty()
                ? cardLine
                : ("Карта #" + transaction.cardId));
        textMcc.setText("5812"); // MCC для категории "Еда"
        if (transaction.cashbackEarned > 0.0001) {
            textCashback.setText(String.format(Locale.US, "+%.2f ₽", transaction.cashbackEarned));
        } else {
            textCashback.setText(String.format(Locale.US, "%.2f ₽", transaction.cashbackEarned));
        }

        if (rowSource != null && textSource != null) {
            if (transaction.source == null || transaction.source.isEmpty()) {
                rowSource.setVisibility(View.GONE);
            } else {
                rowSource.setVisibility(View.VISIBLE);
                textSource.setText(formatTransactionSourceLabel(transaction.source));
            }
        }
        
        // Кнопка закрытия
        dialog.findViewById(R.id.buttonClose).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @NonNull
    private static String formatTransactionSourceLabel(@NonNull String source) {
        switch (source.toLowerCase(Locale.ROOT)) {
            case "demo":
                return "Демо";
            case "import":
                return "Импорт";
            case "manual":
                return "Вручную";
            default:
                return source;
        }
    }

    /** Обновить список после демо-сидинга и т.п., если фрагмент на экране. */
    public void reloadFromServer() {
        if (historyViewModel != null) {
            historyViewModel.loadTransactions();
        }
    }
}





