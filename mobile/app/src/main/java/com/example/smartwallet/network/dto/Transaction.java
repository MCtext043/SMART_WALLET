package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("card_id")
    public int cardId;
    public double amount;
    public String category;
    /** manual | import | demo */
    public String source;
    @SerializedName("external_id")
    public String externalId;
    @SerializedName("occurred_at")
    public String occurredAt;
    @SerializedName("cashback_earned")
    public double cashbackEarned;
    @SerializedName("created_at")
    public String createdAt;
    
    public Transaction() {}
}
