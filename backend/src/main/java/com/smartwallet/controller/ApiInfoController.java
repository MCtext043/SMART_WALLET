package com.smartwallet.controller;

import com.smartwallet.dto.HealthResponse;
import com.smartwallet.dto.RootInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiInfoController {

    @GetMapping("/")
    public RootInfoResponse root() {
        return new RootInfoResponse("SmartWallet API", "1.0.0", "/docs");
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("healthy");
    }
}
