package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.dto.EmployeeRequest;
import com.ysgs.entity.Employee;
import com.ysgs.repository.EmployeeRepository;
import com.ysgs.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/employees")
@PreAuthorize("hasAuthority('admin')")
public class EmployeeManageController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<Employee> getAllEmployees() {
        Integer tenantId = TenantContext.getTenantId();
        return employeeService.getAllEmployees(tenantId);
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeRequest request) {
        Integer tenantId = TenantContext.getTenantId();
        Employee employee = employeeService.createEmployee(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable UUID id, @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id, @RequestParam Boolean isActive) {
        employeeService.toggleActive(id, isActive);
        return ResponseEntity.ok().build();
    }
}