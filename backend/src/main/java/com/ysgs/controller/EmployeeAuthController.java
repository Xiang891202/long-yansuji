package com.ysgs.controller;

import com.ysgs.dto.LoginRequest;
import com.ysgs.dto.LoginResponse;
import com.ysgs.entity.Employee;
import com.ysgs.repository.EmployeeRepository;
import com.ysgs.security.JwtUtils;
import com.ysgs.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/employee")
public class EmployeeAuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        
        // 1. 取得乾淨的真實 IP
        String clientIp = getClientIp(httpRequest);
        System.out.println("Employee IP: " + clientIp);
        
        // 2. 還原並啟用修正後的 IP 限流（已移除 if(false)，正式上線防禦）
        if (loginAttemptService.isIpBlocked(clientIp, "employee")) { 
            throw new RuntimeException("操作频繁，请稍后再试");
        }
        
        String account = request.getIdentityNumber();
        
        // 3. 優先檢查帳號是否鎖定
        if (loginAttemptService.isAccountLocked(account)) {
            long remain = loginAttemptService.getRemainingLockMinutes(account);
            throw new RuntimeException("账号已锁定，请 " + remain + " 分钟后重试");
        }

        Employee employee = employeeRepository.findByIdentityNumber(account).orElse(null);
        boolean passwordMatch = false;
        if (employee != null) {
            passwordMatch = passwordEncoder.matches(request.getPassword(), employee.getPasswordHash());
        }

        if (employee == null || !passwordMatch) {
            loginAttemptService.loginFailed(account);
            int remaining = 5 - loginAttemptService.getFailureCount(account);
            throw new RuntimeException("账号或密码错误，剩余尝试次数：" + remaining);
        }
        
        if (!employee.getIsActive()) {
            throw new RuntimeException("账号已被停用");
        }
        
        loginAttemptService.loginSucceeded(account);

        // 加上這一行：成功登入後，立刻重置該 IP 的限流次數
        loginAttemptService.resetIpCount(clientIp, "employee");
        String token = jwtUtils.generateJwtToken(employee.getId(), "EMPLOYEE", employee.getTenantId(), employee.getPermissions());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", employee.getTenantId(), "EMPLOYEE"));
    }

    /**
     * 同步優化 IP 獲取邏輯，去除 X-Forwarded-For 的可能空白
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            String firstIp = ip.split(",")[0].trim();
            if (!firstIp.isEmpty() && !"unknown".equalsIgnoreCase(firstIp)) {
                return firstIp;
            }
        }
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
