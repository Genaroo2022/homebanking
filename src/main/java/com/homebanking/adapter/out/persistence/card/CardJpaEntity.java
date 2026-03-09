package com.homebanking.adapter.out.persistence.card;

import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_cards_account_id", columnList = "account_id"),
        @Index(name = "idx_cards_last4", columnList = "last4")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "encrypted_number", nullable = false, length = 512)
    private String encryptedNumber;

    @Column(name = "encrypted_cvv", nullable = false, length = 512)
    private String encryptedCvv;

    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @Column(name = "card_holder", nullable = false, length = 120)
    private String cardHolder;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "thru_date", nullable = false)
    private LocalDate thruDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 20)
    private CardColor color;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static CardJpaEntity of(
            UUID id,
            UUID accountId,
            String encryptedNumber,
            String encryptedCvv,
            String last4,
            String cardHolder,
            LocalDate fromDate,
            LocalDate thruDate,
            CardType type,
            CardColor color,
            boolean active,
            Long version) {
        return new CardJpaEntity(
                id,
                accountId,
                encryptedNumber,
                encryptedCvv,
                last4,
                cardHolder,
                fromDate,
                thruDate,
                type,
                color,
                active,
                version
        );
    }
}

