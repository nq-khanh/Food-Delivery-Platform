package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "address_name", length = 50)
    private String addressName;

    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected UserAddress() {}

    public UserAddress(User user, String fullAddress, Point location) {
        this.user = Objects.requireNonNull(user);
        this.fullAddress = requireNonBlank(fullAddress);
        this.location = Objects.requireNonNull(location);
        this.isDefault = false;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getAddressName() { return addressName; }
    public String getFullAddress() { return fullAddress; }
    public Point getLocation() { return location; }
    public boolean isDefault() { return isDefault; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void rename(String addressName) {
        this.addressName = addressName != null ? addressName.trim() : null;
    }

    public void changeAddress(String fullAddress, Point location) {
        this.fullAddress = requireNonBlank(fullAddress);
        this.location = Objects.requireNonNull(location);
    }

    void markAsDefault() {
        this.isDefault = true;
    }

    void unmarkDefault() {
        this.isDefault = false;
    }

    private String requireNonBlank(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be blank");
        }
        return trimmed;
    }

}