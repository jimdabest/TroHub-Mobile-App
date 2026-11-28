package me.ihqqq.trohub.service;

import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    static final int MAX_FAILURES_PER_ACCOUNT_AND_IP = 5;
    static final int MAX_FAILURES_PER_IP = 30;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempt(int count, Instant firstFailureAt) {
        boolean expired(Instant now) {
            return firstFailureAt.plus(WINDOW).isBefore(now);
        }
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String ip, String username) {
        if (exceeded(accountKey(ip, username), MAX_FAILURES_PER_ACCOUNT_AND_IP)
                || exceeded(ipKey(ip), MAX_FAILURES_PER_IP)) {
            throw new AppException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    public void recordFailure(String ip, String username) {
        increment(accountKey(ip, username));
        increment(ipKey(ip));
    }

    public void recordSuccess(String ip, String username) {
        attempts.remove(accountKey(ip, username));
    }

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT10M")
    void purgeExpired() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(e -> e.getValue().expired(now));
    }

    private boolean exceeded(String key, int max) {
        Attempt a = attempts.get(key);
        return a != null && !a.expired(Instant.now()) && a.count() >= max;
    }

    private void increment(String key) {
        attempts.compute(key, (k, a) -> {
            Instant now = Instant.now();
            return (a == null || a.expired(now)) ? new Attempt(1, now) : new Attempt(a.count() + 1, a.firstFailureAt());
        });
    }

    private static String accountKey(String ip, String username) {
        return "a:" + ip + "|" + username;
    }

    private static String ipKey(String ip) {
        return "i:" + ip;
    }
}