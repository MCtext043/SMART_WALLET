package com.example.smartwallet.ui.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartwallet.R;
import com.example.smartwallet.network.dto.Transaction;
import com.example.smartwallet.utils.DateUtils;
import com.example.smartwallet.utils.TransactionCategoryIcons;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private Map<Integer, String> cardLabels = Collections.emptyMap();
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    /** id карты → подпись «Банк · название карты». */
    public void setCardLabels(Map<Integer, String> labels) {
        this.cardLabels = labels != null ? labels : Collections.emptyMap();
        notifyDataSetChanged();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        notifyItemInserted(transactions.size() - 1);
    }

    public void removeTransaction(int position) {
        if (position >= 0 && position < transactions.size()) {
            transactions.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateTransaction(int position, Transaction transaction) {
        if (position >= 0 && position < transactions.size()) {
            transactions.set(position, transaction);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction, cardLabels);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageCategory;
        private final TextView textDate;
        private final TextView textAmount;
        private final TextView textCategory;
        private final TextView textSourceBadge;
        private final TextView textCard;
        private final TextView textCashback;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageCategory = itemView.findViewById(R.id.imageCategory);
            textDate = itemView.findViewById(R.id.textDate);
            textAmount = itemView.findViewById(R.id.textAmount);
            textCategory = itemView.findViewById(R.id.textCategory);
            textSourceBadge = itemView.findViewById(R.id.textSourceBadge);
            textCard = itemView.findViewById(R.id.textCard);
            textCashback = itemView.findViewById(R.id.textCashback);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                int glow = ContextCompat.getColor(itemView.getContext(), R.color.home_card_glow_shadow);
                cardView.setOutlineAmbientShadowColor(glow);
                cardView.setOutlineSpotShadowColor(glow);
            }
        }

        void bind(Transaction transaction, Map<Integer, String> cardLabels) {
            textDate.setText(DateUtils.formatTransactionListTime(transaction));

            textCategory.setText(capitalizeCategory(transaction.category));

            imageCategory.setImageResource(TransactionCategoryIcons.getIconResId(transaction.category));

            textAmount.setText(String.format(Locale.US, "-%.2f ₽", Math.abs(transaction.amount)));

            String cardLine = cardLabels != null ? cardLabels.get(transaction.cardId) : null;
            if (cardLine != null && !cardLine.isEmpty()) {
                textCard.setText(cardLine);
            } else {
                textCard.setText("Карта #" + transaction.cardId);
            }

            if (textSourceBadge != null) {
                String src = transaction.source;
                if (src == null || src.isEmpty()) {
                    textSourceBadge.setVisibility(View.GONE);
                } else {
                    String label;
                    switch (src.toLowerCase(Locale.ROOT)) {
                        case "demo":
                            label = "ДЕМО";
                            break;
                        case "import":
                            label = "ИМПОРТ";
                            break;
                        case "manual":
                            label = "ВРУЧНУЮ";
                            break;
                        default:
                            label = src.toUpperCase(Locale.getDefault());
                    }
                    textSourceBadge.setText(label);
                    textSourceBadge.setVisibility(View.VISIBLE);
                }
            }

            if (transaction.cashbackEarned > 0.0001) {
                textCashback.setVisibility(View.VISIBLE);
                textCashback.setText(String.format(Locale.US, "+%.2f ₽", transaction.cashbackEarned));
            } else {
                textCashback.setVisibility(View.GONE);
            }
        }

        @NonNull
        private static String capitalizeCategory(@Nullable String raw) {
            if (raw == null || raw.isEmpty()) {
                return "";
            }
            String t = raw.trim();
            if (t.isEmpty()) {
                return "";
            }
            int firstCpEnd = t.offsetByCodePoints(0, 1);
            return t.substring(0, firstCpEnd).toUpperCase(Locale.getDefault()) + t.substring(firstCpEnd);
        }
    }
}
