package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    public String phone;
    public String email;
    public String name;
    public int id;
    public String created_at;
    /** Относительный путь или полный URL; если бэкенд не отдаёт — null */
    @SerializedName("avatar_url")
    public String avatarUrl;
}






