package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletTransaction;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.TransactionCreateRequest;
import com.smartwallet.dto.TransactionDto;
import com.smartwallet.exception.ApiException;
import com.smartwallet.repository.CardRepository;
import com.smartwallet.repository.WalletTransactionRepository;
import com.smartwallet.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletTransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public static double calculateCashback(Card card, String category, double amount) {
        Map<String, Integer> rules = card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
        Integer direct = rules.get(category);
        int cashbackPercentage = direct != null ? direct : rules.getOrDefault("прочее", 0);
        if (cashbackPercentage == 0) {
            return 0.0;
        }
        return (amount * cashbackPercentage) / 100.0;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> list(WalletUser user) {
        return transactionRepository.findByUserId(user.getId()).stream()
                .map(DtoMapper::toTransactionDto)
                .toList();
    }

    @Transactional
    public TransactionDto create(WalletUser user, TransactionCreateRequest request) {
        Card card = cardRepository.findByIdAndUserId(request.cardId(), user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Карта не найдена"));
        double cashbackEarned = calculateCashback(card, request.category(), request.amount());
        WalletTransaction tx = WalletTransaction.builder()
                .userId(user.getId())
                .cardId(request.cardId())
                .amount(request.amount())
                .category(request.category())
                .cashbackEarned(cashbackEarned)
                .build();
        tx = transactionRepository.saveAndFlush(tx);
        return DtoMapper.toTransactionDto(tx);
    }
}
