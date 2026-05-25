package com.smartwallet.controller;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.BestCardResponse;
import com.smartwallet.service.CashbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cashback")
@RequiredArgsConstructor
public class CashbackController {

    private final CashbackService cashbackService;

    @GetMapping("/best-card")
    public BestCardResponse best(
            @RequestParam(name = "category") String category,
            @AuthenticationPrincipal WalletUser user
    ) {
        return cashbackService.best(user, category);
    }
}
