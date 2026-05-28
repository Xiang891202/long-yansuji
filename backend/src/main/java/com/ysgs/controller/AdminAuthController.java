package com.ysgs.controller;

import com.ysgs.dto.LoginRequest;
import com.ysgs.dto.LoginResponse;
import com.ysgs.entity.Admin;
import com.ysgs.repository.AdminRepository;
import com.ysgs.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
public class AdminAuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Admin admin = adminRepository
                .findByTenantIdAndEmailAndPhone(request.getTenantId(), request.getEmail(), request.getPhone())
                .orElseThrow(() -> new RuntimeException("帳號或密碼錯誤"));

        List<String> permissions = List.of("admin");

        String token = jwtUtils.generateJwtToken(
                admin.getId(),
                "ADMIN",
                admin.getTenantId(),
                permissions
        );

        return ResponseEntity.ok(new LoginResponse(token, "Bearer", admin.getTenantId(), "ADMIN"));
    }
}