package com.smartwallet.repository;

import com.smartwallet.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    List<Recommendation> findTop3ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Integer userId, Instant after);
}
