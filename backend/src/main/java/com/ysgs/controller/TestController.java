package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/test/db")
    public ResponseEntity<?> testDb() {
        long count = employeeRepository.count();
        return ResponseEntity.ok("Tenant: " + TenantContext.getTenantId() + ", Employee count: " + count);
    }
}