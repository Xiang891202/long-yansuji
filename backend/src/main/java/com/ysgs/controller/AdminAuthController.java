package com.ysgs.controller;

import com.ysgs.dto.LoginRequest;
import com.ysgs.dto.LoginResponse;
import com.ysgs.entity.Admin;
import com.ysgs.repository.AdminRepository;
import com.ysgs.security.JwtUtils;
import com.ysgs.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
public class AdminAuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        
        // 1. 取得乾淨、標準化的客戶端真實 IP
        String clientIp = getClientIp(httpRequest);
        System.out.println(" [DEBUG] 管理員登入嘗試，解析後的 IP 為: " + clientIp);

        // 2. 使用統一的限流服務 (搭配已修正的 Service)
        if (loginAttemptService.isIpBlocked(clientIp, "admin")) {
            throw new RuntimeException("操作频繁，请稍后再试");
        }
        
        String account = request.getEmail();
        
        // 3. 優先檢查帳號是否鎖定
        if (loginAttemptService.isAccountLocked(account)) {
            long remain = loginAttemptService.getRemainingLockMinutes(account);
            throw new RuntimeException("账号已锁定，请 " + remain + " 分钟后重试");
        }

        // 4. 驗證帳號密碼
        Admin admin = adminRepository.findByEmail(account).orElse(null);
        boolean passwordMatch = (admin != null) && passwordEncoder.matches(request.getPassword(), admin.getPasswordHash());

        if (admin == null || !passwordMatch) {
            loginAttemptService.loginFailed(account);
            int failures = loginAttemptService.getFailureCount(account);
            int remaining = (failures >= 5) ? 0 : (5 - failures);
            throw new RuntimeException("账号或密码错误，剩余尝试次数：" + remaining);
        }

        loginAttemptService.loginSucceeded(account);

        // 加上這一行：成功登入後，立刻重置該 IP 的限流次數
        loginAttemptService.resetIpCount(clientIp, "admin"); 
        List<String> permissions = List.of("admin");
        String token = jwtUtils.generateJwtToken(admin.getId(), "ADMIN", admin.getTenantId(), permissions);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", admin.getTenantId(), "ADMIN"));
    }

    /**
     * 修正後的 IP 獲取方法：增加去空白、過濾未知、防範多層反向代理的健全邏輯
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多個 IP 時取第一個，並使用 trim() 去除 Nginx/Cloudflare 可能產生的空白字元
            String firstIp = ip.split(",")[0].trim();
            if (!firstIp.isEmpty() && !"unknown".equalsIgnoreCase(firstIp)) {
                return firstIp;
            }
        }
        
        // 備用方案：嘗試其他常見的代理 Header
        ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        return ip != null ? ip.trim() : "0.0.0.0";
    }
}
