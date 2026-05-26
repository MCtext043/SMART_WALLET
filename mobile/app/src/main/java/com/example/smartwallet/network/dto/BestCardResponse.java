package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class BestCardResponse {
    @SerializedName("card_id")
    public int cardId;
    
    @SerializedName("bank_name")
    public String bankName;
    
    @SerializedName("card_name")
    public String cardName;
    
    @SerializedName("cashback_percentage")
    public int cashbackPercentage;
    
    public String category;
    
    public BestCardResponse() {}
}
