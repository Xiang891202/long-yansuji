package com.ysgs.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginAttemptService {

    // ========== 使用 Caffeine Cache 代替 HashMap，解決線上記憶體溢出問題 ==========
    
    // 帳號登入失敗次數累加器：從最後一次寫入起算 15 分鐘後自動過期清除
    private final Cache<String, Integer> FAIL_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    // 帳號鎖定截止時間戳：寫入後 15 分鐘自動過期清除（對應鎖定時長 15 分鐘）
    private final Cache<String, Long> LOCK_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    // IP 限流對象：5 分鐘內完全沒有任何新登入請求，自動從記憶體中蒸發，防範惡意 IP 掃描
    private final Cache<String, IpCounter> IP_COUNT_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    // 內部結構：同時保存時間窗起點與安全執行緒的計數器
    private static class IpCounter {
        long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        IpCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private static final long IP_WINDOW_MS = 60_000;      // 1分鐘
    private static final int IP_MAX_ATTEMPTS = 10;        // 1分鐘最多10次
    private static final int ACCOUNT_MAX_FAILURES = 5;    // 密碼錯5次鎖定
    private static final long LOCK_DURATION_MINUTES = 15; // 鎖定15分鐘

    // ========== IP 限流業務方法 ==========
    public boolean isIpBlocked(String ip, String loginType) {
        String key = loginType + ":" + ip;   
        long now = System.currentTimeMillis();
        
        // 核心修正：從 Caffeine Cache 獲取，若不存在則利用 Lambda 自動初始化
        IpCounter counter = IP_COUNT_CACHE.get(key, k -> new IpCounter(now));
        
        synchronized (counter) {
            // 如果超過一分鐘，重置時間視窗起點與次數
            if (now - counter.windowStart > IP_WINDOW_MS) {
                counter.windowStart = now;
                counter.count.set(1); 
                return false;
            }
        }
        
        int currentCount = counter.count.incrementAndGet();
        System.out.println(" [DEBUG] IP: " + key + " 當前一分鐘內嘗試次數: " + currentCount);
        
        return currentCount > IP_MAX_ATTEMPTS;
    }

    // 成功登入後，立刻從快取中清除該 IP 紀錄，避免累積次數
    public void resetIpCount(String ip, String loginType) {
        String key = loginType + ":" + ip;
        IP_COUNT_CACHE.invalidate(key);
        System.out.println(" [DEBUG] IP: " + key + " 成功登入，已清空該 IP 限流計數器。");
    }

    // ========== 帳號鎖定業務方法 ==========
    public void loginFailed(String account) {
        // 若找不到則回傳預設值 0
        int failures = FAIL_CACHE.get(account, k -> 0);
        failures++;
        FAIL_CACHE.put(account, failures);
        System.out.println(" [DEBUG] 帳號 " + account + " 失敗次數: " + failures);
        
        if (failures >= ACCOUNT_MAX_FAILURES) {
            long lockUntil = System.currentTimeMillis() + LOCK_DURATION_MINUTES * 60 * 1000;
            LOCK_CACHE.put(account, lockUntil);
            System.out.println(" [DEBUG] 帳號 " + account + " 已鎖定至: " + new java.util.Date(lockUntil));
        }
    }

    public void loginSucceeded(String account) {
        // 使用 invalidate 代替原本 Map 的 remove
        FAIL_CACHE.invalidate(account);
        LOCK_CACHE.invalidate(account);
    }

    public boolean isAccountLocked(String account) {
        Long lockUntil = LOCK_CACHE.getIfPresent(account);
        if (lockUntil != null && lockUntil > System.currentTimeMillis()) return true;
        
        // 如果鎖定時間到了，自動釋放快取中的數據
        if (lockUntil != null && lockUntil <= System.currentTimeMillis()) {
            FAIL_CACHE.invalidate(account);
            LOCK_CACHE.invalidate(account);
        }
        return false;
    }

    public long getRemainingLockMinutes(String account) {
        Long lockUntil = LOCK_CACHE.getIfPresent(account);
        if (lockUntil == null) return 0;
        long remaining = lockUntil - System.currentTimeMillis();
        if (remaining <= 0) return 0;
        return (long) Math.ceil(remaining / 60000.0);
    }

    public int getFailureCount(String account) {
        Integer count = FAIL_CACHE.getIfPresent(account);
        return count == null ? 0 : count;
    }
}
