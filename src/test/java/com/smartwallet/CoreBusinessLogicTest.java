package com.smartwallet;

import com.smartwallet.domain.Card;
import com.smartwallet.service.AssistantChatService;
import com.smartwallet.service.TransactionService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoreBusinessLogicTest {

    @Test
    void calculateCashback_usesExactCategoryOtherwiseOther() {
        Card card =
                Card.builder()
                        .cashbackRules(Map.of("еда", 5, "прочее", 1))
                        .build();
        assertThat(TransactionService.calculateCashback(card, "еда", 2000)).isEqualTo(100.0);
        assertThat(TransactionService.calculateCashback(card, "транспорт", 100)).isEqualTo(1.0);
    }

    @Test
    void calculateCashback_returnsZeroWhenNoApplicableRule() {
        Card emptyRules = Card.builder().cashbackRules(new LinkedHashMap<>()).build();
        Card onlyFood = Card.builder().cashbackRules(Map.of("еда", 3)).build();
        assertThat(TransactionService.calculateCashback(emptyRules, "еда", 100)).isZero();
        assertThat(TransactionService.calculateCashback(onlyFood, "прочее", 100)).isZero();
    }

    @Test
    void cleanGigaResponse_extractsContentAndNormalizesEscapes() {
        String reply =
                AssistantChatService.cleanGigaResponse("{\"content\":\"Строка: тест\\nДальше\"}");
        assertThat(reply).contains("Строка: тест").contains("\n").doesNotContain("\\n");

        assertThat(AssistantChatService.stripTrailingZeros(2000.0)).isEqualTo("2000");
        assertThat(AssistantChatService.stripTrailingZeros(10.25)).contains("10.25");
    }
}
