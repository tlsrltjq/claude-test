package com.eactive.resourcehub.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LoginAttemptService {

    static final int MAX_ATTEMPTS = 10;

    private final ConcurrentHashMap<String, AtomicInteger> cache = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        cache.remove(normalize(email));
    }

    public void loginFailed(String email) {
        cache.computeIfAbsent(normalize(email), k -> new AtomicInteger(0)).incrementAndGet();
    }

    public boolean isOverLimit(String email) {
        AtomicInteger count = cache.get(normalize(email));
        return count != null && count.get() >= MAX_ATTEMPTS;
    }

    public int getFailCount(String email) {
        AtomicInteger count = cache.get(normalize(email));
        return count == null ? 0 : count.get();
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
