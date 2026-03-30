package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.PayoutRequestStatus;
import com.hkt.fooddelivery.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payout_requests")
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayoutRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private WalletTransaction transaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected PayoutRequest() {}

    protected PayoutRequest(Wallet wallet, BigDecimal amount) {
        this.wallet = Objects.requireNonNull(wallet);
        this.amount = validateAmount(amount);
        this.status = PayoutRequestStatus.PENDING;
    }

    public static PayoutRequest create(Wallet wallet, BigDecimal amount) {
        return new PayoutRequest(wallet, amount);
    }

    public UUID getId() { return id; }
    public Wallet getWallet() { return wallet; }
    public BigDecimal getAmount() { return amount; }
    public PayoutRequestStatus getStatus() { return status; }
    public String getNote() { return note; }
    public WalletTransaction getTransaction() { return transaction; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void markApproved(WalletTransaction tx) {
        if (this.status != PayoutRequestStatus.PENDING) {
            throw new BusinessException("Only pending request can be approved");
        }

        Objects.requireNonNull(tx);

        if (!tx.getWallet().equals(this.wallet)) {
            throw new BusinessException("Transaction does not belong to this wallet");
        }

        this.transaction = tx;
        this.status = PayoutRequestStatus.APPROVED;
    }

    public void reject(String note) {
        if (this.status != PayoutRequestStatus.PENDING) {
            throw new BusinessException("Only pending request can be rejected");
        }

        this.status = PayoutRequestStatus.REJECTED;
        this.note = normalize(note);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.signum() <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}