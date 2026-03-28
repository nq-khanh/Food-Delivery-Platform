package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Column(name = "price_at_purchase", precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;

    public Integer getId() { return id;}
    public Order getOrder() { return order;}
    public Product getProduct() { return product;}
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase;}
    public int getQuantity() { return quantity;}

    protected OrderItem() {}

    OrderItem(Order order, Product product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = price;
    }

    public BigDecimal getTotalPrice() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }

    void addQuantity(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        this.quantity += additionalQuantity;
    }
}