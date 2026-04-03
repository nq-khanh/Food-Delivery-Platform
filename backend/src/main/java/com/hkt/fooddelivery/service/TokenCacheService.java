package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.entity.enums.Role;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenCacheService {
    // Thời gian sống của token (ví dụ 15 phút)
    private static final long TOKEN_TTL_MINUTES = 15;

    // Key: Token (String), Value: Token metadata
    private final ConcurrentHashMap<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    public String createRegistrationToken(String email, Role role) {
        Objects.requireNonNull(role);
        if (role != Role.USER && role != Role.MERCHANT && role != Role.SHIPPER) {
            throw new IllegalArgumentException("Unsupported registration role");
        }

        String token = java.util.UUID.randomUUID().toString();
        long expireAt = Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60).getEpochSecond();
        tokenStore.put(token, new TokenEntry(email, expireAt, TokenPurpose.REGISTRATION, role));
        return token;
    }

    public RegistrationTokenPayload getRegistrationPayloadByToken(String token) {
        TokenEntry entry = getValidEntry(token, TokenPurpose.REGISTRATION);
        if (entry == null || entry.role() == null) {
            return null;
        }
        return new RegistrationTokenPayload(entry.email(), entry.role());
    }

    public String createPasswordResetToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        long expireAt = Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60).getEpochSecond();
        tokenStore.put(token, new TokenEntry(email, expireAt, TokenPurpose.PASSWORD_RESET, null));
        return token;
    }

    public String getPasswordResetEmailByToken(String token) {
        TokenEntry entry = getValidEntry(token, TokenPurpose.PASSWORD_RESET);
        if (entry == null) {
            return null;
        }
        return entry.email();
    }

    public void deleteToken(String token) {
        tokenStore.remove(token);
    }

    private TokenEntry getValidEntry(String token, TokenPurpose expectedPurpose) {
        TokenEntry entry = tokenStore.get(token);
        if (entry == null || Instant.now().getEpochSecond() > entry.expireAt() || entry.purpose() != expectedPurpose) {
            tokenStore.remove(token);
            return null;
        }
        return entry;
    }

    public record RegistrationTokenPayload(String email, Role role) {}

    private record TokenEntry(String email, long expireAt, TokenPurpose purpose, Role role) {}

    private enum TokenPurpose {
        REGISTRATION,
        PASSWORD_RESET
    }
}