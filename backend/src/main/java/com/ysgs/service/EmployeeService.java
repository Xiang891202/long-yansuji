package com.ysgs.service;

import com.ysgs.dto.EmployeeRequest;
import com.ysgs.entity.Employee;
import com.ysgs.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees(Integer tenantId) {
        return employeeRepository.findByTenantId(tenantId);
    }

    public Employee getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("員工不存在"));
    }

    @Transactional
    public Employee createEmployee(Integer tenantId, EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        employee.setIdentityNumber(request.getIdentityNumber());
        employee.setBirthDate(request.getBirthDate());
        employee.setName(request.getName());
        employee.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        employee.setPermissions(request.getPermissions());
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(UUID id, EmployeeRequest request) {
        Employee employee = getEmployeeById(id);
        employee.setIdentityNumber(request.getIdentityNumber());
        employee.setBirthDate(request.getBirthDate());
        employee.setName(request.getName());
        employee.setIsActive(request.getIsActive());
        employee.setPermissions(request.getPermissions());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);
    }

    @Transactional
    public void toggleActive(UUID id, Boolean isActive) {
        Employee employee = getEmployeeById(id);
        employee.setIsActive(isActive);
        employeeRepository.save(employee);
    }
}