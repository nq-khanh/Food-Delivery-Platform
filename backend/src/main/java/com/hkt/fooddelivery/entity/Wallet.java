package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_holder", length = 100)
    private String bankAccountHolder;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getBalance() { return balance; }
    public String getBankName() { return bankName; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public String getBankAccountHolder() { return bankAccountHolder; }
    public Instant getUpdatedAt() { return updatedAt; }

    protected Wallet() {
    }

    public Wallet(User user) {
        this.user = Objects.requireNonNull(user);
        this.balance = BigDecimal.ZERO.setScale(2);
        this.updatedAt = Instant.now();
    }

    public void credit(BigDecimal amount) {
        amount = normalizeAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        amount = normalizeAmount(amount);

        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        this.balance = this.balance.subtract(amount);
    }

    public void updateBankInfo(String name, String number, String holder) {
        this.bankName = requireNonBlank(name);
        this.bankAccountNumber = requireNonBlank(number);
        this.bankAccountHolder = requireNonBlank(holder);
    }

    public void clearBankInfo() {
        this.bankName = null;
        this.bankAccountNumber = null;
        this.bankAccountHolder = null;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String requireNonBlank(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
        return trimmed;
    }
}
