package com.hkt.fooddelivery.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenCacheService {
    // Thời gian sống của token (ví dụ 15 phút)
    private static final long TOKEN_TTL_MINUTES = 15;

    // Key: Token (String), Value: Email (String)
    private final ConcurrentHashMap<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        long expireAt = Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60).getEpochSecond();
        tokenStore.put(token, new TokenEntry(email, expireAt));
        return token;
    }

    public String getEmailByToken(String token) {
        TokenEntry entry = tokenStore.get(token);
        if (entry == null || Instant.now().getEpochSecond() > entry.expireAt()) {
            tokenStore.remove(token);
            return null;
        }
        return entry.email();
    }

    public void deleteToken(String token) {
        tokenStore.remove(token);
    }

    private record TokenEntry(String email, long expireAt) {}
}