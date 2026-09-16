package com.unified.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenManager {

    private final Cache<String, Boolean> blacklist = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    private final Cache<Long, String> latestToken = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    public void addBlacklist(String token) {
        blacklist.put(token, true);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(blacklist.getIfPresent(token));
    }

    public void updateLatestToken(Long userId, String token) {
        latestToken.put(userId, token);
    }

    public boolean isLatestToken(Long userId, String token) {
        String latest = latestToken.getIfPresent(userId);
        return latest == null || latest.equals(token);
    }

    public void removeToken(Long userId) {
        latestToken.invalidate(userId);
    }
}
