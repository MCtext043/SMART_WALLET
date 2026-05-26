package com.smartwallet.util;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.Recommendation;
import com.smartwallet.domain.WalletTransaction;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.*;

public final class DtoMapper {

    private DtoMapper() {}

    public static UserDto toUserDto(WalletUser u) {
        return new UserDto(u.getId(), u.getPhone(), u.getEmail(), u.getName(), u.getCreatedAt());
    }

    public static CardDto toCardDto(Card c) {
        return new CardDto(
                c.getId(),
                c.getUserId(),
                c.getBankName(),
                c.getCardName(),
                c.getLast4(),
                c.getCashbackRules(),
                c.getLimitMonthly(),
                c.getCreatedAt()
        );
    }

    public static TransactionDto toTransactionDto(WalletTransaction t) {
        return new TransactionDto(
                t.getId(),
                t.getUserId(),
                t.getCardId(),
                t.getAmount(),
                t.getCategory(),
                t.getCashbackEarned(),
                t.getCreatedAt()
        );
    }

    public static RecommendationDto toRecommendationDto(Recommendation r) {
        return new RecommendationDto(r.getId(), r.getUserId(), r.getMessage(), r.getType(), r.getCreatedAt());
    }
}
