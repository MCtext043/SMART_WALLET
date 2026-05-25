package com.smartwallet.controller;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.TransactionCreateRequest;
import com.smartwallet.dto.TransactionDto;
import com.smartwallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping({"", "/"})
    public List<TransactionDto> list(@AuthenticationPrincipal WalletUser user) {
        return transactionService.list(user);
    }

    @PostMapping({"", "/"})
    public TransactionDto create(
            @AuthenticationPrincipal WalletUser user,
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        return transactionService.create(user, request);
    }
}
