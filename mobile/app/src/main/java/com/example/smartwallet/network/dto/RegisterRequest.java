package com.example.smartwallet.network.dto;

public class RegisterRequest {
    private final String phone;
    private final String email;
    private final String name;
    private final String password;

    public RegisterRequest(String phone, String email, String name, String password) {
        this.phone = phone;
        this.email = email;
        this.name = name;
        this.password = password;
    }
}


