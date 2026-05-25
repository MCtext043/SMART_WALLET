package com.smartwallet.controller;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.TokenResponse;
import com.smartwallet.dto.UserDto;
import com.smartwallet.dto.UserLoginRequest;
import com.smartwallet.dto.UserRegisterRequest;
import com.smartwallet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody UserLoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/profile")
    public UserDto profile(@AuthenticationPrincipal WalletUser user) {
        return authService.profile(user);
    }
}
