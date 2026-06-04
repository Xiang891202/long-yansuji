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
        // 使用統一的限流服務
        String clientIp = getClientIp(httpRequest);
        if (loginAttemptService.isIpBlocked(clientIp, "admin")) {
            throw new RuntimeException("操作频繁，请稍后再试");
        }
        String account = request.getEmail();
        // 2. 優先檢查帳號是否鎖定
        if (loginAttemptService.isAccountLocked(account)) {
            long remain = loginAttemptService.getRemainingLockMinutes(account);
            throw new RuntimeException("账号已锁定，请 " + remain + " 分钟后重试");
        }

        // 3. 驗證帳號密碼
        Admin admin = adminRepository.findByEmail(account).orElse(null);
        boolean passwordMatch = (admin != null) && passwordEncoder.matches(request.getPassword(), admin.getPasswordHash());

        if (admin == null || !passwordMatch) {
            loginAttemptService.loginFailed(account);
            int failures = loginAttemptService.getFailureCount(account);
            int remaining = (failures >= 5) ? 0 : (5 - failures);
            throw new RuntimeException("账号或密码错误，剩余尝试次数：" + remaining);
        }

        loginAttemptService.loginSucceeded(account);
        List<String> permissions = List.of("admin");
        String token = jwtUtils.generateJwtToken(admin.getId(), "ADMIN", admin.getTenantId(), permissions);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", admin.getTenantId(), "ADMIN"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}