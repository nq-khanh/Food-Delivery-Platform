package com.hkt.fooddelivery.entity;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Wallet() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankAccountHolder() { return bankAccountHolder; }
    public void setBankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = bankAccountHolder; }

    public Instant getUpdatedAt() { return updatedAt; }

    public static WalletBuilder builder() { return new WalletBuilder(); }

    public static final class WalletBuilder {
        private User user;
        private BigDecimal balance = BigDecimal.ZERO;
        private String bankName;
        private String bankAccountNumber;
        private String bankAccountHolder;

        public WalletBuilder user(User user) { this.user = user; return this; }
        public WalletBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public WalletBuilder bankName(String bankName) { this.bankName = bankName; return this; }
        public WalletBuilder bankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; return this; }
        public WalletBuilder bankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = bankAccountHolder; return this; }

        public Wallet build() {
            Wallet w = new Wallet();
            w.setUser(user);
            w.setBalance(balance != null ? balance : BigDecimal.ZERO);
            w.setBankName(bankName);
            w.setBankAccountNumber(bankAccountNumber);
            w.setBankAccountHolder(bankAccountHolder);
            return w;
        }
    }
}
