package com.ysgs.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginAttemptService {

    // 失敗計數（email/identityNumber）
    private static final ConcurrentHashMap<String, Integer> FAIL_MAP = new ConcurrentHashMap<>();
    // 鎖定截止時間戳
    private static final ConcurrentHashMap<String, Long> LOCK_MAP = new ConcurrentHashMap<>();
    
    // ========== 修正：改存自訂的計數對象 ==========
    private static final ConcurrentHashMap<String, IpCounter> IP_COUNT_MAP = new ConcurrentHashMap<>();
    private static final long IP_WINDOW_MS = 300_000; // 改為 5 分鐘 (300,000 毫秒)
    private static final int IP_MAX_ATTEMPTS = 20;     // 5 分鐘內最多允許 20 次嘗試
 

    private static final int ACCOUNT_MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MINUTES = 15; 

    // 定義一個結構體存放時間和次數
    private static class IpCounter {
        long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        IpCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    // ========== 修正後的 IP 限流方法 ==========
    public boolean isIpBlocked(String ip, String loginType) {
        String key = loginType + ":" + ip;   
        long now = System.currentTimeMillis();
        
        // 取得或初始化該 IP 的計數器
        IpCounter counter = IP_COUNT_MAP.computeIfAbsent(key, k -> new IpCounter(now));
        
        synchronized (counter) {
            // 如果超過一分鐘，重置時間戳與次數
            if (now - counter.windowStart > IP_WINDOW_MS) {
                counter.windowStart = now;
                counter.count.set(1); // 本次算第一次
                return false;
            }
        }
        
        // 累加次數並判斷是否超過最大限制
        int currentCount = counter.count.incrementAndGet();
        System.out.println(" [DEBUG] IP: " + key + " 當前一分鐘內嘗試次數: " + currentCount);
        
        return currentCount > IP_MAX_ATTEMPTS;
    }

    // ========== 新增：成功登入時重置 IP 計數的方法 ==========
    public void resetIpCount(String ip, String loginType) {
        String key = loginType + ":" + ip;
        // 直接從 Map 中移除該 IP 的計數紀錄，使其下一次嘗試時重新從 0 開始計算
        IP_COUNT_MAP.remove(key);
        // System.out.println(" [DEBUG] IP: " + key + " 已成功登入，重置該 IP 的限流計數器。");
    }

    // ========== 帳號鎖定方法 (保持不變) ==========
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
