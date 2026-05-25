package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletTransaction;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.ChatMessageRequest;
import com.smartwallet.dto.ChatResponseDto;
import com.smartwallet.gateway.GigachatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantChatService {

    private final GigachatClient gigachatClient;

    public ChatResponseDto chat(WalletUser currentUser, List<Card> cards, List<WalletTransaction> recentFive, ChatMessageRequest message) {
        String userContext = buildUserContext(currentUser, cards, recentFive);

        try {
            String systemMessage =
                    """
                            Ты учитель по финансовой грамотности, объясняй четко и понятно.
                            Ты помогаешь пользователю SmartWallet оптимизировать кэшбэк с карт.

                            Контекст пользователя:
                            %s

                            Отвечай коротко и по делу. Если пользователь спрашивает про кэшбэк, используй информацию о его картах."""
                            .stripIndent()
                            .formatted(userContext);

            Map<String, Object> body = gigachatRequestBody(systemMessage, message.message());

            var responseEntity = gigachatClient.postMessage(body);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return new ChatResponseDto(cleanGigaResponse(responseEntity.getBody()));
            }
            return new ChatResponseDto(
                    "Извините, произошла ошибка при обращении к ассистенту (код "
                            + responseEntity.getStatusCode().value()
                            + "). Попробуйте позже."
            );
        } catch (org.springframework.web.client.ResourceAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                return new ChatResponseDto("Извините, ассистент не отвечает. Попробуйте позже.");
            }
            log.debug("Assistant connection failed", e);
            return new ChatResponseDto(
                    "Извините, не удается подключиться к ассистенту. Проверьте интернет-соединение."
            );
        } catch (Exception e) {
            log.warn("Assistant chat failure", e);
            return new ChatResponseDto("Произошла ошибка: " + e.getMessage());
        }
    }

    static Map<String, Object> gigachatRequestBody(String systemMessage, String userMessage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", "GigaChat:latest");
        payload.put("stream", false);
        payload.put("update_interval", 0);
        payload.put(
                "messages",
                List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                )
        );
        payload.put("n", 1);
        payload.put("max_tokens", 256);
        payload.put("repetition_penalty", 1.0);
        return payload;
    }

    static String buildUserContext(WalletUser currentUser, List<Card> cards, List<WalletTransaction> recentFive) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пользователь: ").append(currentUser.getName()).append('\n');
        sb.append("Телефон: ").append(currentUser.getPhone()).append("\n\n");

        if (cards != null && !cards.isEmpty()) {
            sb.append("Карты пользователя:\n");
            for (Card card : cards) {
                Map<String, Integer> cashbackRules =
                        card.getCashbackRules() == null ? Map.of() : card.getCashbackRules();
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

    /** Удобочитаемый вывод сумм в контексте (избегаем научной нотации). */
    public static String stripTrailingZeros(Double v) {
        if (v == null) return "0";
        if (v == Math.rint(v)) {
            return String.valueOf(v.longValue());
        }
        return String.format(Locale.US, "%s", v);
    }

    public static String cleanGigaResponse(String responseText) {
        String text;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode gigaData = mapper.readTree(responseText);
            if (gigaData.isObject() && gigaData.hasNonNull("content")) {
                text = gigaData.get("content").asText();
            } else {
                text = responseText;
            }
        } catch (Exception e) {
            text = responseText;
        }
        text = text.strip();
        text = text.replace("\\n", "\n");
        text = text.replace("\\\"", "\"");
        text = text.replace("\\/", "/");
        text = text.replace("\\\\", "\\");
        text = text.replace("\\t", "\t");
        return text;
    }
}
