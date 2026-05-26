package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.Recommendation;
import com.smartwallet.domain.WalletTransaction;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.RecommendationDto;
import com.smartwallet.repository.CardRepository;
import com.smartwallet.repository.RecommendationRepository;
import com.smartwallet.repository.WalletTransactionRepository;
import com.smartwallet.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final CardRepository cardRepository;
    private final WalletTransactionRepository txnRepository;
    private final Clock clock;

    @Transactional
    public List<RecommendationDto> getRecommendations(WalletUser user) {
        var cutoff = clock.instant().minus(1, ChronoUnit.DAYS);
        var cached =
                recommendationRepository.findTop3ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(user.getId(), cutoff);
        if (!cached.isEmpty()) {
            return cached.stream().map(DtoMapper::toRecommendationDto).toList();
        }
        List<Recommendation> generated = generatePersonalized(user);
        recommendationRepository.saveAll(generated);
        return generated.stream().map(DtoMapper::toRecommendationDto).toList();
    }

    private List<Recommendation> generatePersonalized(WalletUser currentUser) {
        Integer userId = currentUser.getId();

        List<Recommendation> recommendations = new ArrayList<>();
        List<Card> userCards = cardRepository.findByUserId(userId);
        List<WalletTransaction> recentTransactions =
                txnRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        if (userCards.isEmpty()) {
            recommendations.add(
                    rec(userId, "Добавьте карты для получения кэшбэка и оптимизации трат", "совет"));
        } else if (userCards.size() == 1) {
            recommendations.add(
                    rec(userId,
                            "Добавьте больше карт для разных категорий трат - это увеличит ваш кэшбэк",
                            "совет"));
        }

        if (!recentTransactions.isEmpty()) {
            Map<String, Double> categorySpending = new LinkedHashMap<>();
            for (WalletTransaction tx : recentTransactions) {
                categorySpending.merge(tx.getCategory(), tx.getAmount(), Double::sum);
            }

            if (!categorySpending.isEmpty()) {
                Map.Entry<String, Double> top = categorySpending.entrySet().stream()
                        .max(Map.Entry.<String, Double>comparingByValue())
                        .orElseThrow();
                String topCategory = top.getKey();
                double topAmount = top.getValue();

                Card bestCardForTop = null;
                int bestCashbackPct = 0;
                for (Card card : userCards) {
                    int cashbackPercentage = cashbackPercent(card, topCategory);
                    if (cashbackPercentage > bestCashbackPct) {
                        bestCashbackPct = cashbackPercentage;
                        bestCardForTop = card;
                    }
                }

                if (bestCardForTop != null && bestCashbackPct > 0) {
                    double potentialCashback = (topAmount * bestCashbackPct) / 100.0;
                    recommendations.add(rec(
                            userId,
                            "Для категории '%s' (%s\u20BD) используйте %s (%d%%) - получите %s\u20BD кэшбэка"
                                    .formatted(
                                            topCategory,
                                            formatAmount(topAmount),
                                            bestCardForTop.getBankName(),
                                            bestCashbackPct,
                                            formatAmount(potentialCashback)),
                            "совет"
                    ));
                }

                double totalSpent =
                        recentTransactions.stream().mapToDouble(WalletTransaction::getAmount).sum();
                double totalCashback =
                        recentTransactions.stream().mapToDouble(WalletTransaction::getCashbackEarned).sum();
                double cashbackRatio = totalSpent > 0 ? (totalCashback / totalSpent * 100.0) : 0;

                if (cashbackRatio < 1) {
                    recommendations.add(rec(
                            userId,
                            String.format(
                                    Locale.US,
                                    "Ваш кэшбэк составляет %.1f%% от трат. Рассмотрите карты с более высоким кэшбэком",
                                    cashbackRatio),
                            "совет"
                    ));
                }

                LinkedHashSet<String> allCategories = new LinkedHashSet<>();
                for (Card card : userCards) {
                    if (card.getCashbackRules() != null) {
                        allCategories.addAll(card.getCashbackRules().keySet());
                    }
                }
                LinkedHashSet<String> usedCategories = new LinkedHashSet<>(categorySpending.keySet());
                List<String> unused = allCategories.stream()
                        .filter(c -> !usedCategories.contains(c))
                        .sorted()
                        .toList();
                if (!unused.isEmpty()) {
                    recommendations.add(rec(
                            userId,
                            "Используйте карты для категорий: %s - они дают дополнительный кэшбэк"
                                    .formatted(String.join(", ", unused)),
                            "совет"
                    ));
                }
            }
        }

        if (recommendations.size() < 2) {
            recommendations.add(rec(userId,
                    "Проверяйте лимиты кэшбэка перед крупными покупками",
                    "совет"));
            recommendations.add(rec(userId,
                    "Используйте разные карты для разных категорий трат",
                    "совет"));
        }

        for (Card card : userCards) {
            if (card.getLimitMonthly() != null && card.getLimitMonthly() != 0.0) {
                List<WalletTransaction> cardTx = txnRepository.findByUserIdAndCardId(userId, card.getId());
                double currentCashback =
                        cardTx.stream().mapToDouble(WalletTransaction::getCashbackEarned).sum();
                if (currentCashback > card.getLimitMonthly() * 0.8) {
                    recommendations.add(rec(
                            userId,
                            "Внимание: кэшбэк по карте %s (%s\u20BD из %s\u20BD) близок к лимиту"
                                    .formatted(
                                            card.getBankName(),
                                            formatAmount(currentCashback),
                                            formatAmount(card.getLimitMonthly())),
                            "акция"
                    ));
                }
            }
        }

        int n = Math.min(3, recommendations.size());
        return new ArrayList<>(recommendations.subList(0, n));
    }

    private static Recommendation rec(Integer userId, String message, String type) {
        return Recommendation.builder().userId(userId).message(message).type(type).build();
    }

    private static int cashbackPercent(Card card, String category) {
        Map<String, Integer> cashbackRules =
                card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
        Integer direct = cashbackRules.get(category);
        return direct != null ? direct : cashbackRules.getOrDefault("прочее", 0);
    }

    /** Целые суммы в рублях в текстах рекомендаций (формат {:.0f}). */
    private static String formatAmount(double amount) {
        return String.format(Locale.US, "%.0f", amount);
    }
}
