package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class DemoSeedResponse {
    public boolean reset;
    @SerializedName("cards_created")
    public int cardsCreated;
    @SerializedName("transactions_created")
    public int transactionsCreated;
    @SerializedName("recommendations_removed")
    public int recommendationsRemoved;
}
