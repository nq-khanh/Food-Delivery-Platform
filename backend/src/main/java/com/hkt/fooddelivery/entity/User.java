package com.hkt.fooddelivery.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.*;

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

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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
