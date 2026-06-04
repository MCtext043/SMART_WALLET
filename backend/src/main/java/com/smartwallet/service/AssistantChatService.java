package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletTransaction;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.ChatMessageRequest;
import com.smartwallet.dto.ChatResponseDto;
import com.smartwallet.gateway.OllamaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantChatService {

    private final OllamaClient ollamaClient;

    public ChatResponseDto chat(
            WalletUser currentUser,
            List<Card> cards,
            List<WalletTransaction> recentFive,
            ChatMessageRequest message
    ) {
        String userContext = buildUserContext(currentUser, cards, recentFive);

        try {
            String systemMessage =
                    """
                            Ты финансовый ассистент приложения Smart Wallet на русском языке.
                            Помогаешь с кэшбэком, картами, тратами и финансовой грамотностью.
                            Отвечай кратко (до 5 предложений), по делу, без выдуманных данных.

                            Контекст пользователя:
                            %s"""
                            .stripIndent()
                            .formatted(userContext);

            String reply = ollamaClient.chat(systemMessage, message.message());
            if (reply != null && !reply.isBlank()) {
                return new ChatResponseDto(reply);
            }
            log.warn("Ollama returned empty reply");
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warn("Ollama unreachable: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Ollama error", e);
        }

        return new ChatResponseDto(
                AssistantFallbackResponder.reply(message.message(), cards, recentFive)
        );
    }

    static String buildUserContext(WalletUser currentUser, List<Card> cards, List<WalletTransaction> recentFive) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пользователь: ").append(currentUser.getName()).append('\n');
        sb.append("Телефон: ").append(currentUser.getPhone()).append("\n\n");

        if (cards != null && !cards.isEmpty()) {
            sb.append("Карты пользователя:\n");
            for (Card card : cards) {
                var cashbackRules = card.getCashbackRules() == null ? java.util.Map.<String, Integer>of() : card.getCashbackRules();
                String rulesStr =
                        cashbackRules.entrySet().stream()
                                .map(e -> "%s: %s%%".formatted(e.getKey(), e.getValue()))
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("");
                sb.append("- ")
                        .append(card.getBankName())
                        .append(' ')
                        .append(card.getCardName())
                        .append(" (****")
                        .append(card.getLast4())
                        .append("): ")
                        .append(rulesStr)
                        .append('\n');
            }
            sb.append('\n');
        }

        if (recentFive != null && !recentFive.isEmpty()) {
            sb.append("Последние транзакции:\n");
            for (WalletTransaction trans : recentFive) {
                sb.append("- ")
                        .append(trans.getCategory())
                        .append(": ")
                        .append(stripTrailingZeros(trans.getAmount()))
                        .append('\u20BD')
                        .append(" (кэшбэк: ")
                        .append(stripTrailingZeros(trans.getCashbackEarned()))
                        .append('\u20BD')
                        .append(")\n");
            }
        }

        return sb.toString();
    }

    public static String stripTrailingZeros(Double v) {
        if (v == null) return "0";
        if (v == Math.rint(v)) {
            return String.valueOf(v.longValue());
        }
        return String.format(Locale.US, "%s", v);
    }
}
