package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "vnp_txn_ref", unique = true, length = 100)
    private String txnRef;

    @Column(name = "vnp_transaction_no", length = 100)
    private String transactionNo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Payment() {}

    public Payment(Order order, BigDecimal amount, String txnRef) {
        this.order = Objects.requireNonNull(order);
        this.amount = validateAmount(amount);
        this.txnRef = Objects.requireNonNull(txnRef);

        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public String getTxnRef() { return txnRef; }
    public String getTransactionNo() { return transactionNo; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public Instant getCreatedAt() { return createdAt; }

    public void markSuccess(String transactionNo) {
        if (paymentStatus != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already processed");
        }

        this.paymentStatus = PaymentStatus.SUCCESS;
        this.transactionNo = Objects.requireNonNull(transactionNo);
    }

    public void markFailed() {
        if (paymentStatus != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already processed");
        }

        this.paymentStatus = PaymentStatus.FAILED;
    }

    public boolean isSuccess() {
        return paymentStatus == PaymentStatus.SUCCESS;
    }


    private BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        return amount;
    }
}
