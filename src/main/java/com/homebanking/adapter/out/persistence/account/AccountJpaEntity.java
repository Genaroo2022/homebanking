package com.homebanking.adapter.out.persistence.account;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountJpaEntity {

    public static final int CBU_LENGTH = 22;

    @Setter(AccessLevel.PACKAGE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false, length = CBU_LENGTH)
    private String cbu;

    @Column(unique = true, nullable = false)
    private String alias;

    @Column(nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    public AccountJpaEntity(UUID userId, String cbu, String alias,
                            BigDecimal balance, LocalDateTime createdAt) {
        this.userId = userId;
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.createdAt = createdAt;
        this.version = 0L;
    }
}
