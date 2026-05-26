package com.smartwallet.controller;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.CardCreateRequest;
import com.smartwallet.dto.CardDto;
import com.smartwallet.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping({"", "/"})
    public List<CardDto> list(@AuthenticationPrincipal WalletUser user) {
        return cardService.list(user);
    }

    @PostMapping({"", "/"})
    public CardDto create(@AuthenticationPrincipal WalletUser user, @Valid @RequestBody CardCreateRequest request) {
        return cardService.create(user, request);
    }

    @GetMapping("/{cardId}")
    public CardDto get(@AuthenticationPrincipal WalletUser user, @PathVariable int cardId) {
        return cardService.getById(user, cardId);
    }
}
