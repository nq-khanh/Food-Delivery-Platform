package com.hkt.fooddelivery.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "system_configs")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected SystemConfig() {}

    public SystemConfig(String key, String value, String description) {
        this.key = requireKey(key);
        this.value = requireValue(value);
        this.description = normalize(description);
    }


    public Integer getId() { return id; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
    public Instant getUpdatedAt() { return updatedAt; }


    public void updateValue(String newValue) {
        this.value = requireValue(newValue);
    }

    public void updateDescription(String description) {
        this.description = normalize(description);
    }


    private String requireKey(String key) {
        Objects.requireNonNull(key);
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be blank");
        }
        return trimmed;
    }

    private String requireValue(String value) {
        Objects.requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
        return trimmed;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}