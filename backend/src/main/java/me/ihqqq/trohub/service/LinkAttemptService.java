package me.ihqqq.trohub.service;

import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Giới hạn nhập sai mã liên kết theo tài khoản (5 lần / 15 phút), chống dò mã. Lưu trong bộ nhớ, đủ cho 1 instance. */
@Service
public class LinkAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempt(int count, Instant firstFailureAt) {
        boolean expired(Instant now) {
            return firstFailureAt.plus(WINDOW).isBefore(now);
        }
    }

    private final ConcurrentHashMap<UUID, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(UUID userId) {
        Attempt a = attempts.get(userId);
        if (a != null && !a.expired(Instant.now()) && a.count() >= MAX_FAILURES) {
            throw new AppException(ErrorCode.TOO_MANY_LINK_ATTEMPTS);
        }
    }

    public void recordFailure(UUID userId) {
        attempts.compute(userId, (k, a) -> {
            Instant now = Instant.now();
            return (a == null || a.expired(now)) ? new Attempt(1, now) : new Attempt(a.count() + 1, a.firstFailureAt());
        });
    }

    public void recordSuccess(UUID userId) {
        attempts.remove(userId);
    }

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT10M")
    void purgeExpired() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(e -> e.getValue().expired(now));
    }
}