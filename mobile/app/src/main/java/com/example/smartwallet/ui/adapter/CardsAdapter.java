package com.example.smartwallet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartwallet.R;
import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.ui.BankCardStyle;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    
    private List<Card> cards = new ArrayList<>();
    private OnCardClickListener onCardClickListener;
    
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }
    
    public void setOnCardClickListener(OnCardClickListener listener) {
        this.onCardClickListener = listener;
    }
    
    public void setCards(List<Card> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }

    @Nullable
    public Card getCardAt(int position) {
        if (position < 0 || position >= cards.size()) return null;
        return cards.get(position);
    }

    public int getCardCount() {
        return cards.size();
    }
    
    public void addCard(Card card) {
        cards.add(card);
        notifyItemInserted(cards.size() - 1);
    }
    
    public void removeCard(int position) {
        if (position >= 0 && position < cards.size()) {
            cards.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateCard(int position, Card card) {
        if (position >= 0 && position < cards.size()) {
            cards.set(position, card);
            notifyItemChanged(position);
        }
    }
    
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(cards.get(position));
    }
    
    @Override
    public int getItemCount() {
        return cards.size();
    }
    
    class CardViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView textBankName;
        private TextView textCardName;
        private TextView textLast4;
        private TextView textCashbackLimit;
        
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textBankName = itemView.findViewById(R.id.textBankName);
            textCardName = itemView.findViewById(R.id.textCardName);
            textLast4 = itemView.findViewById(R.id.textLast4);
            textCashbackLimit = itemView.findViewById(R.id.textCashbackLimit);
            
            itemView.setOnClickListener(v -> {
                if (onCardClickListener != null) {
                    onCardClickListener.onCardClick(cards.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Card card) {
            BankCardStyle style = BankCardStyle.forCard(card);
            style.apply(cardView);

            int primary = style.primaryTextColor(itemView.getContext());
            int secondary = style.secondaryTextColor(itemView.getContext());
            textBankName.setTextColor(primary);
            textCardName.setTextColor(primary);
            textLast4.setTextColor(primary);
            textCashbackLimit.setTextColor(secondary);

            textBankName.setText(card.bankName);
            textCardName.setText(card.cardName);
            textLast4.setText("•••• " + card.last4);
            textCashbackLimit.setText("Лимит: " + String.format("%.0f", card.limitMonthly) + " ₽");
        }
    }
}
