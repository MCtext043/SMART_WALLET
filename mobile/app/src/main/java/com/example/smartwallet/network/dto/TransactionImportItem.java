package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class TransactionImportItem {
    public double amount;
    public String category;
    @SerializedName("card_last4")
    public String cardLast4;
    @SerializedName("external_id")
    public String externalId;
    @SerializedName("occurred_at")
    public String occurredAt;

    public TransactionImportItem(double amount, String category, String cardLast4) {
        this.amount = amount;
        this.category = category;
        this.cardLast4 = cardLast4;
    }
}
