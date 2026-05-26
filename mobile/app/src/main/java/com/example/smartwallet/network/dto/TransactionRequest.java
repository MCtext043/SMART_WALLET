package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    public double amount;
    public String category;
    
    @SerializedName("card_id")
    public int cardId;
    
    public TransactionRequest() {}
    
    public TransactionRequest(double amount, String category, int cardId) {
        this.amount = amount;
        this.category = category;
        this.cardId = cardId;
    }
}
