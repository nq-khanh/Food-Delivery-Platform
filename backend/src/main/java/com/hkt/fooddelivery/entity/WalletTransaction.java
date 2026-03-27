package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.WalletTransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "transaction_code", nullable = false, unique = true)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletTransactionType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    protected WalletTransaction() {}

    protected WalletTransaction(
            Wallet wallet,
            Order order,
            WalletTransactionType type,
            BigDecimal amount,
            String description,
            String transactionCode
    ) {
        this.wallet = Objects.requireNonNull(wallet);
        this.order = order;
        this.type = Objects.requireNonNull(type);
        this.amount = validateAmount(amount);
        this.description = normalizeDescription(description);
        this.transactionCode = Objects.requireNonNull(transactionCode);
    }

    public UUID getId() { return id; }
    public Wallet getWallet() { return wallet; }
    public Order getOrder() { return order; }
    public String getTransactionCode() { return transactionCode; }
    public WalletTransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }

    private BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Amount must not be zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeDescription(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}