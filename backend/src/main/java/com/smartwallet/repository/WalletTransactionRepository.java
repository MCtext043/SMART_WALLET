package com.smartwallet.repository;

import com.smartwallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {

    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<WalletTransaction> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);

    List<WalletTransaction> findTop10ByUserIdOrderByCreatedAtDesc(Integer userId);

    List<WalletTransaction> findByUserId(Integer userId);

    List<WalletTransaction> findByUserIdAndCardId(Integer userId, Integer cardId);
}
