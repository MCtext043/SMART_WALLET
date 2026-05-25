package com.smartwallet.service;

import com.smartwallet.domain.Card;
import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.CardCreateRequest;
import com.smartwallet.dto.CardDto;
import com.smartwallet.exception.ApiException;
import com.smartwallet.repository.CardRepository;
import com.smartwallet.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public List<CardDto> list(WalletUser user) {
        return cardRepository.findByUserId(user.getId()).stream().map(DtoMapper::toCardDto).toList();
    }

    @Transactional
    public CardDto create(WalletUser user, CardCreateRequest request) {
        Card card = Card.builder()
                .userId(user.getId())
                .bankName(request.bankName())
                .cardName(request.cardName())
                .last4(request.last4())
                .cashbackRules(request.cashbackRules())
                .limitMonthly(request.limitMonthly())
                .build();
        card = cardRepository.saveAndFlush(card);
        return DtoMapper.toCardDto(card);
    }

    @Transactional(readOnly = true)
    public CardDto getById(WalletUser user, int cardId) {
        Card card = cardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Карта не найдена"));
        return DtoMapper.toCardDto(card);
    }
}
