package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class CardCashbackPatchRequest {
    @SerializedName("cashback_rules")
    public Map<String, Integer> cashbackRules;
}
