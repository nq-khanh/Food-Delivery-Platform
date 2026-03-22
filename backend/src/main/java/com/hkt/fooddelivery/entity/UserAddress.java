package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

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

    @Column(name = "location", nullable = false, columnDefinition = "TEXT")
    private String location;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UserAddress() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


    public static UserAddressBuilder builder() { return new UserAddressBuilder(); }

    public static final class UserAddressBuilder {
        private User user;
        private String addressName;
        private String fullAddress;
        private String location;
        private boolean isDefault;

        public UserAddressBuilder user(User user) { this.user = user; return this; }
        public UserAddressBuilder addressName(String addressName) { this.addressName = addressName; return this; }
        public UserAddressBuilder fullAddress(String fullAddress) { this.fullAddress = fullAddress; return this; }
        public UserAddressBuilder location(String location) { this.location = location; return this; }
        public UserAddressBuilder isDefault(boolean isDefault) { this.isDefault = isDefault; return this; }

        public UserAddress build() {
            UserAddress a = new UserAddress();
            a.setUser(user);
            a.setAddressName(addressName);
            a.setFullAddress(fullAddress);
            a.setLocation(location);
            a.setDefault(isDefault);
            return a;
        }
    }
}
