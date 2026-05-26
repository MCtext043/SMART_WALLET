package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Card {
    @SerializedName("id")
    public int id;
    
    @SerializedName("user_id")
    public int userId;
    
    @SerializedName("bank_name")
    public String bankName;
    
    @SerializedName("card_name")
    public String cardName;
    
    @SerializedName("last4")
    public String last4;
    
    @SerializedName("cashback_rules")
    public Map<String, Integer> cashbackRules;
    
    @SerializedName("limit_monthly")
    public double limitMonthly;
    
    @SerializedName("created_at")
    public String createdAt;
    
    public Card() {}
}
