package com.ysgs.controller;

import com.ysgs.dto.LoginRequest;
import com.ysgs.dto.LoginResponse;
import com.ysgs.entity.Employee;
import com.ysgs.repository.EmployeeRepository;
import com.ysgs.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth/employee")
public class EmployeeAuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 驗證身分證與生日
        Employee employee = employeeRepository
            .findByTenantIdAndIdentityNumberAndBirthDate(request.getTenantId(), request.getIdentityNumber(), request.getBirthDate())
            .orElseThrow(() -> new RuntimeException("帳號或密碼錯誤"));

        if (!employee.getIsActive()) {
            throw new RuntimeException("帳號已被停用");
        }

        // 產生 JWT，role 固定為 "EMPLOYEE"
        String token = jwtUtils.generateJwtToken(
                employee.getId(),
                "EMPLOYEE",
                employee.getTenantId(),
                employee.getPermissions()
        );

        return ResponseEntity.ok(new LoginResponse(token, "Bearer", employee.getTenantId(), "EMPLOYEE"));
    }
}