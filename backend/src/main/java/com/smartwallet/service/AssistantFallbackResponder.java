package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletTransaction;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Локальные ответы ассистента, когда внешний GigaChat-прокси недоступен с сервера.
 */
final class AssistantFallbackResponder {

    private AssistantFallbackResponder() {}

    static String reply(String userMessage, List<Card> cards, List<WalletTransaction> recent) {
        String q = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT).strip();

        if (cards == null || cards.isEmpty()) {
            if (q.contains("карт") || q.contains("кэшбэк") || q.contains("кешбэк")) {
                return "Добавьте банковские карты в разделе «Карты» и укажите правила кэшбэка по категориям — "
                        + "тогда я смогу подсказать, какой картой платить выгоднее.";
            }
        }

        if (q.contains("кэшбэк") || q.contains("кешбэк") || q.contains("карт") || q.contains("оплат")) {
            return cashbackAdvice(cards, q);
        }

        if (q.contains("трат") || q.contains("расход") || q.contains("бюджет") || q.contains("аналит")) {
            return spendingAdvice(recent);
        }

        if (q.contains("привет") || q.contains("здрав") || q.isEmpty()) {
            return greeting(cards);
        }

        return defaultAdvice(cards, recent);
    }

    private static String greeting(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "Здравствуйте! Я финансовый ассистент Smart Wallet. "
                    + "Добавьте карты с правилами кэшбэка — подскажу, как получать больше возврата с покупок.";
        }
        return "Здравствуйте! Вижу у вас " + cards.size() + " "
                + pluralCards(cards.size())
                + ". Спросите, какой картой платить в категории (еда, транспорт, покупки) или как улучшить кэшбэк.";
    }

    private static String cashbackAdvice(List<Card> cards, String question) {
        if (cards == null || cards.isEmpty()) {
            return "Сначала добавьте карты в приложении. Для каждой укажите проценты кэшбэка по категориям.";
        }

        String category = detectCategory(question);
        Card best = null;
        int bestPct = -1;

        for (Card card : cards) {
            int pct = percentFor(card, category);
            if (pct > bestPct) {
                bestPct = pct;
                best = card;
            }
        }

        if (best == null || bestPct <= 0) {
            return "Проверьте правила кэшбэка на картах. Для категории «"
                    + category
                    + "» задайте ставку или используйте категорию «прочее». "
                    + "На главном экране есть подбор лучшей карты перед оплатой.";
        }

        return "Для «" + category + "» сейчас выгоднее "
                + best.getBankName() + " " + best.getCardName()
                + " (•••• " + best.getLast4() + ") — кэшбэк " + bestPct + "%.\n"
                + cardsSummary(cards);
    }

    private static String spendingAdvice(List<WalletTransaction> recent) {
        if (recent == null || recent.isEmpty()) {
            return "Пока нет транзакций. После покупок в «Истории» появится аналитика — "
                    + "следите за категориями и выбирайте карту с максимальным кэшбэком.";
        }
        double total = 0;
        double cashback = 0;
        String topCat = recent.get(0).getCategory();
        double topSum = 0;
        Map<String, Double> byCat = new java.util.LinkedHashMap<>();
        for (WalletTransaction t : recent) {
            total += t.getAmount();
            cashback += t.getCashbackEarned();
            byCat.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        for (var e : byCat.entrySet()) {
            if (e.getValue() > topSum) {
                topSum = e.getValue();
                topCat = e.getKey();
            }
        }
        double ratio = total > 0 ? (cashback / total * 100.0) : 0;
        return "По последним операциям больше всего трат в категории «" + topCat + "».\n"
                + String.format(Locale.US, "Сумма трат: %.0f ₽, кэшбэк: %.0f ₽ (%.1f%% от трат).\n", total, cashback, ratio)
                + "Используйте раздел «Аналитика» и перед оплатой — рекомендацию карты на главном экране.";
    }

    private static String defaultAdvice(List<Card> cards, List<WalletTransaction> recent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Сейчас отвечаю в упрощённом режиме (внешний AI временно недоступен на сервере). ");
        sb.append("Могу подсказать по кэшбэку, картам и тратам в Smart Wallet.\n\n");
        if (cards != null && !cards.isEmpty()) {
            sb.append(cardsSummary(cards));
        }
        if (recent != null && !recent.isEmpty()) {
            sb.append("\nПоследняя операция: ")
                    .append(recent.get(0).getCategory())
                    .append(" — ")
                    .append(AssistantChatService.stripTrailingZeros(recent.get(0).getAmount()))
                    .append(" ₽.");
        }
        return sb.toString();
    }

    private static String cardsSummary(List<Card> cards) {
        StringBuilder sb = new StringBuilder("Ваши карты:\n");
        for (Card c : cards) {
            Map<String, Integer> rules = c.getCashbackRules() == null ? Map.of() : c.getCashbackRules();
            String rulesStr = rules.entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue() + "%")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("правила не заданы");
            sb.append("• ").append(c.getBankName()).append(" ").append(c.getCardName())
                    .append(" — ").append(rulesStr).append("\n");
        }
        return sb.toString().strip();
    }

    private static String detectCategory(String q) {
        if (q.contains("еда") || q.contains("продукт") || q.contains("ресторан") || q.contains("кафе")) {
            return "еда";
        }
        if (q.contains("транспорт") || q.contains("такси") || q.contains("метро") || q.contains("бензин")) {
            return "транспорт";
        }
        if (q.contains("развлеч") || q.contains("кино")) {
            return "развлечения";
        }
        if (q.contains("здоров") || q.contains("аптек")) {
            return "здоровье";
        }
        if (q.contains("одежд") || q.contains("маркет") || q.contains("покупк")) {
            return "покупки";
        }
        return "прочее";
    }

    private static int percentFor(Card card, String category) {
        Map<String, Integer> rules = card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
        Integer direct = rules.get(category);
        if (direct != null) {
            return direct;
        }
        return rules.getOrDefault("прочее", 0);
    }

    private static String pluralCards(int n) {
        int mod10 = n % 10;
        int mod100 = n % 100;
        if (mod100 >= 11 && mod100 <= 14) {
            return "карт";
        }
        if (mod10 == 1) {
            return "карта";
        }
        if (mod10 >= 2 && mod10 <= 4) {
            return "карты";
        }
        return "карт";
    }
}
