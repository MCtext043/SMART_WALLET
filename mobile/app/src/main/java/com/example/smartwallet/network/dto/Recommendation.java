package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class Recommendation {
    public String message;
    public String type;
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("created_at")
    public String createdAt;
    
    public Recommendation() {}
}
