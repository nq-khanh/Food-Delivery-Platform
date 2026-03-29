package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.hkt.fooddelivery.entity.enums.Role;
import com.hkt.fooddelivery.entity.enums.TokenType;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserToken> tokens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected User() {}

    public User(String username, String email, String phone, String firstName, String lastName, String passwordHash) {
        this.username = requireNonBlank(username).toLowerCase();
        this.email = normalizeEmail(email);
        this.phone = requireNonBlank(phone);
        this.firstName = requireNonBlank(firstName);
        this.lastName = requireNonBlank(lastName);
        this.passwordHash = Objects.requireNonNull(passwordHash);

        this.role = Role.USER;
        this.isActive = true;
        this.isVerified = false;
    }


    public UUID getId() { return id; }
    public String getUsername() {return  username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isVerified() { return isVerified; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<UserAddress> getAddresses() {
        return List.copyOf(addresses);
    }

    public List<UserToken> getTokens() {
        return List.copyOf(tokens);
    }

    public void changeEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public void changePhone(String phone) {
        this.phone = requireNonBlank(phone);
    }

    public void changeProfile(String firstName, String lastName) {
        this.firstName = requireNonBlank(firstName);
        this.lastName = requireNonBlank(lastName);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void verify() {
        if (!isActive) {
            throw new IllegalStateException("Cannot verify inactive user");
        }
        if (isVerified) return;
        this.isVerified = true;
    }

    public void changeRole(Role role) {
        Objects.requireNonNull(role);
        this.role = role;
    }

    public void changePassword(String hashedPassword) {
        this.passwordHash = Objects.requireNonNull(hashedPassword);
    }

    public void addAddress(String fullAddress, Point location, boolean isDefault) {
        if (this.addresses.size() >= 5) {
            throw new IllegalStateException("Maximum 5 addresses allowed per user");
        }

        UserAddress newAddress = new UserAddress(this, fullAddress, location);

        if (isDefault || this.addresses.isEmpty()) {
            makeDefault(newAddress);
        }
        this.addresses.add(newAddress);
    }

    public void setDefaultAddress(UUID addressId) {
        UserAddress target = this.addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        makeDefault(target);
    }

    public void addToken(String tokenHash, TokenType type, Instant expiresAt) {
        this.tokens.removeIf(t -> t.isExpired() || t.isRevoked());

        UserToken newToken = new UserToken(this, tokenHash, type, expiresAt);
        this.tokens.add(newToken);
    }

    private void makeDefault(UserAddress target) {
        this.addresses.forEach(UserAddress::unmarkDefault);
        target.markAsDefault();
    }

    public void revokeAllTokens() {
        this.tokens.forEach(UserToken::revoke);
    }


    private String normalizeEmail(String email) {
        Objects.requireNonNull(email);
        String value = email.trim().toLowerCase();
        if (!value.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        return value;
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
