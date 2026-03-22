package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, columnDefinition = "TEXT")
    private String tokenHash;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked")
    private boolean isRevoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UserToken() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return isRevoked; }
    public void setRevoked(boolean revoked) { isRevoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }

    public static UserTokenBuilder builder() { return new UserTokenBuilder(); }

    public static final class UserTokenBuilder {
        private User user;
        private String tokenHash;
        private String type;
        private Instant expiresAt;
        private boolean isRevoked;

        public UserTokenBuilder user(User user) { this.user = user; return this; }
        public UserTokenBuilder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public UserTokenBuilder type(String type) { this.type = type; return this; }
        public UserTokenBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public UserTokenBuilder isRevoked(boolean isRevoked) { this.isRevoked = isRevoked; return this; }

        public UserToken build() {
            UserToken t = new UserToken();
            t.setUser(user);
            t.setTokenHash(tokenHash);
            t.setType(type);
            t.setExpiresAt(expiresAt);
            t.setRevoked(isRevoked);
            return t;
        }
    }
}
