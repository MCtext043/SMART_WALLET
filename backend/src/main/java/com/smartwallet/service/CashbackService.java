package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.BestCardResponse;
import com.smartwallet.exception.ApiException;
import com.smartwallet.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CashbackService {

    private final CardRepository cardRepository;

    private static int percentForCategory(Card card, String category) {
        Map<String, Integer> cashbackRules =
                card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
        Integer direct = cashbackRules.get(category);
        return direct != null ? direct : cashbackRules.getOrDefault("прочее", 0);
    }

    @Transactional(readOnly = true)
    public BestCardResponse best(WalletUser user, String category) {
        List<Card> userCards = cardRepository.findByUserId(user.getId());
        if (userCards.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "У вас нет добавленных карт");
        }

        Card bestCard = null;
        int bestCashback = 0;
        String resultCategory = category;

        for (Card card : userCards) {
            int cashbackPercentage = percentForCategory(card, category);
            if (cashbackPercentage > bestCashback) {
                bestCashback = cashbackPercentage;
                bestCard = card;
            }
        }

        if (bestCard == null) {
            for (Card card : userCards) {
                Map<String, Integer> cashbackRules =
                        card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
                int cashbackPercentage = cashbackRules.getOrDefault("прочее", 0);
                if (cashbackPercentage > bestCashback) {
                    bestCashback = cashbackPercentage;
                    bestCard = card;
                    resultCategory = "прочее";
                }
            }
        }

        if (bestCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Не найдена подходящая карта");
        }

        return new BestCardResponse(
                bestCard.getId(),
                bestCard.getBankName(),
                bestCard.getCardName(),
                bestCashback,
                resultCategory
        );
    }
}
