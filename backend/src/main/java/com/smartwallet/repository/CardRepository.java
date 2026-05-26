package com.smartwallet.repository;

import com.smartwallet.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Integer> {

    List<Card> findByUserId(Integer userId);

    Optional<Card> findByIdAndUserId(Integer id, Integer userId);
}
