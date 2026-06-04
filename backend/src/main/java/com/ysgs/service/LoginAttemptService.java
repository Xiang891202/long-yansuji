package com.ysgs.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    // 失敗計數（email/identityNumber）
    private static final ConcurrentHashMap<String, Integer> FAIL_MAP = new ConcurrentHashMap<>();
    // 鎖定截止時間戳
    private static final ConcurrentHashMap<String, Long> LOCK_MAP = new ConcurrentHashMap<>();
    
    // IP 限流（1分鐘內最多10次）
    private static final ConcurrentHashMap<String, Long> IP_COUNT_MAP = new ConcurrentHashMap<>();
    private static final long IP_WINDOW_MS = 60_000;
    
    private static final int IP_MAX_ATTEMPTS = 20; // 測試用，正式可改10

    private static final int ACCOUNT_MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MINUTES = 15; // 測試用，正式可改15

    // ========== IP 限流方法 ==========
    // 修改 isIpBlocked 方法
    public boolean isIpBlocked(String ip, String loginType) {
        String key = loginType + ":" + ip;   // 例如 "admin:127.0.0.1" 和 "employee:127.0.0.1"
        long now = System.currentTimeMillis();
        Long lastReset = IP_COUNT_MAP.computeIfAbsent(key, k -> now);
        if (now - lastReset > IP_WINDOW_MS) {
            IP_COUNT_MAP.put(key, now);
            return false;
        }
        long count = IP_COUNT_MAP.merge(key, 1L, Long::sum);
        return count > IP_MAX_ATTEMPTS;
    }

    // ========== 帳號鎖定方法 ==========
    public void loginFailed(String account) {
        int failures = FAIL_MAP.getOrDefault(account, 0);
        failures++;
        FAIL_MAP.put(account, failures);
        System.out.println(" [DEBUG] 失敗次數: " + failures);
        if (failures >= ACCOUNT_MAX_FAILURES) {
            long lockUntil = System.currentTimeMillis() + LOCK_DURATION_MINUTES * 60 * 1000;
            LOCK_MAP.put(account, lockUntil);
            System.out.println(" [DEBUG] 帳號已鎖定至: " + new java.util.Date(lockUntil));
        }
    }

    public void loginSucceeded(String account) {
        FAIL_MAP.remove(account);
        LOCK_MAP.remove(account);
    }

    public boolean isAccountLocked(String account) {
        Long lockUntil = LOCK_MAP.get(account);
        if (lockUntil != null && lockUntil > System.currentTimeMillis()) return true;
        if (lockUntil != null && lockUntil <= System.currentTimeMillis()) {
            LOCK_MAP.remove(account);
            FAIL_MAP.remove(account);
        }
        return false;
    }

    public long getRemainingLockMinutes(String account) {
        Long lockUntil = LOCK_MAP.get(account);
        if (lockUntil == null) return 0;
        long remaining = lockUntil - System.currentTimeMillis();
        if (remaining <= 0) return 0;
        return (long) Math.ceil(remaining / 60000.0);
    }

    public int getFailureCount(String account) {
        return FAIL_MAP.getOrDefault(account, 0);
    }
}