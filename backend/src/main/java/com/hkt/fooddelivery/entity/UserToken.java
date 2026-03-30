package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hkt.fooddelivery.entity.enums.TokenType;
import com.hkt.fooddelivery.exception.BusinessException;
import jakarta.persistence.*;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "token_hash", nullable = false, columnDefinition = "TEXT")
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TokenType type;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked")
    private boolean isRevoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public TokenType getType() { return type; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return isRevoked; }
    public Instant getCreatedAt() { return createdAt; }

    protected UserToken() {}

    UserToken(User user, String tokenHash, TokenType type, Instant expiresAt) {
        this.user = Objects.requireNonNull(user);
        this.tokenHash = requireNonBlank(tokenHash);
        this.type = Objects.requireNonNull(type);
        this.expiresAt = Objects.requireNonNull(expiresAt);

        if (expiresAt.isBefore(Instant.now())) {
            throw new BusinessException("Expiration must be in the future");
        }

        this.isRevoked = false;
    }

    void revoke() {
        if (this.isRevoked) return;
        this.isRevoked = true;
    }

    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isRevoked && !isExpired();
    }

    private String requireNonBlank(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException("Value cannot be blank");
        }
        return trimmed;
    }
}
