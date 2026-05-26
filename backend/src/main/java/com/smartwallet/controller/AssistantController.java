package com.smartwallet.controller;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.ChatMessageRequest;
import com.smartwallet.dto.ChatResponseDto;
import com.smartwallet.dto.RecommendationDto;
import com.smartwallet.repository.CardRepository;
import com.smartwallet.repository.WalletTransactionRepository;
import com.smartwallet.service.AssistantChatService;
import com.smartwallet.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final RecommendationService recommendationService;
    private final AssistantChatService assistantChatService;
    private final CardRepository cardRepository;
    private final WalletTransactionRepository transactionRepository;

    @GetMapping("/recommendations")
    public List<RecommendationDto> recommendations(@AuthenticationPrincipal WalletUser user) {
        return recommendationService.getRecommendations(user);
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(
            @AuthenticationPrincipal WalletUser user,
            @Valid @RequestBody ChatMessageRequest message
    ) {
        var cards = cardRepository.findByUserId(user.getId());
        var recent = transactionRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());
        return assistantChatService.chat(user, cards, recent, message);
    }
}
