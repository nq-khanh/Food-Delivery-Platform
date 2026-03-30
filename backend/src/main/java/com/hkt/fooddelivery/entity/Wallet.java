package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.hkt.fooddelivery.entity.enums.WalletTransactionType;
import com.hkt.fooddelivery.exception.BusinessException;
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

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletTransaction> transactions = new ArrayList<>();

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Wallet() {
    }

    public Wallet(User user) {
        this.user = Objects.requireNonNull(user);
        this.balance = BigDecimal.ZERO.setScale(2);
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getBalance() { return balance; }
    public String getBankName() { return bankName; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public String getBankAccountHolder() { return bankAccountHolder; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<WalletTransaction> getTransactions() {
        return List.copyOf(transactions);
    }


    public void credit(BigDecimal amount, Order order, String description) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(order, "Order is required for credit");

        if (amount.signum() <= 0) {
            throw new BusinessException("Amount must be positive");
        }

        this.balance = normalize(this.balance.add(amount));

        WalletTransaction tx = new WalletTransaction(
                this,
                order,
                WalletTransactionType.ORDER_REVENUE,
                amount,
                description,
                generateCode()
        );

        this.transactions.add(tx);
    }

    public WalletTransaction debitForPayout(BigDecimal amount, PayoutRequest request) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(request);

        if (amount.signum() <= 0) {
            throw new BusinessException("Amount must be positive");
        }

        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        this.balance = normalize(this.balance.subtract(amount));

        WalletTransaction tx = new WalletTransaction(
                this,
                null,
                WalletTransactionType.WITHDRAWAL,
                amount.negate(),
                "Payout: " + request.getId(),
                generateCode()
        );

        this.transactions.add(tx);

        return tx;
    }

    public void debit(BigDecimal amount, WalletTransactionType type, String description) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(type, "Transaction type is required");

        if (amount.signum() <= 0) {
            throw new BusinessException("Amount must be positive");
        }

        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        if (type == WalletTransactionType.ORDER_REVENUE) {
            throw new BusinessException("Invalid transaction type for debit");
        }

        this.balance = normalize(this.balance.subtract(amount));

        WalletTransaction tx = new WalletTransaction(
                this,
                null,
                type,
                amount.negate(),
                description,
                generateCode()
        );

        this.transactions.add(tx);
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

    public void receiveOrderRevenue(Order order, BigDecimal amount) {
        credit(amount, order, "Order revenue: " + order.getOrderCode());
    }

    public void withdraw(BigDecimal amount) {
        debit(amount, WalletTransactionType.WITHDRAWAL, "Withdraw");
    }

    public void refund(Order order, BigDecimal amount) {
        debit(amount, WalletTransactionType.REFUND, "Refund order: " + order.getOrderCode());
    }

    private String requireNonBlank(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException("Value cannot be blank");
        }
        return trimmed;
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateCode() {
        return "TX-" + UUID.randomUUID();
    }
}
