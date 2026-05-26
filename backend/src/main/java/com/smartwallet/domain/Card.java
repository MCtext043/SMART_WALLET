package com.smartwallet.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "card_name")
    private String cardName;

    @Column(length = 4)
    private String last4;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cashback_rules", columnDefinition = "jsonb")
    private Map<String, Integer> cashbackRules;

    @Column(name = "limit_monthly")
    private Double limitMonthly;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
