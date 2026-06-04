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
import java.util.concurrent.ConcurrentHashMap;

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
        // 使用統一的限流服務
        String clientIp = getClientIp(httpRequest);
        System.out.println("Employee IP: " + clientIp);
        // 临时禁用 IP 限流，以便测试账号锁定功能
        if (false) { 
            System.out.println("Blocked: " + loginAttemptService.isIpBlocked(clientIp, "employee"));
            throw new RuntimeException("操作频繁，请稍后再试");
        }
        String account = request.getIdentityNumber();
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
        String token = jwtUtils.generateJwtToken(employee.getId(), "EMPLOYEE", employee.getTenantId(), employee.getPermissions());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", employee.getTenantId(), "EMPLOYEE"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}