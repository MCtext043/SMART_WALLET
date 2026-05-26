package com.example.smartwallet.network.dto;

public class LoginRequest {
    private final String phone;
    private final String password;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}


