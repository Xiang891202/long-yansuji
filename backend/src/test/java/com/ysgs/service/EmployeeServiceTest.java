package com.ysgs.service;

import com.ysgs.dto.EmployeeRequest;
import com.ysgs.entity.Employee;
import com.ysgs.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequest request;
    private final Integer tenantId = 2;
    private final UUID employeeId = UUID.randomUUID();
    private final String rawPassword = "test123";
    private final String encodedPassword = "bcrypt_hash_of_test123";

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);
        employee.setTenantId(tenantId);
        employee.setIdentityNumber("A123456789");
        // employee.setBirthDate(LocalDate.of(1990, 1, 1));  // birthDate 已不再使用
        employee.setName("張三");
        employee.setIsActive(true);
        employee.setPermissions(List.of("inventory_access"));
        employee.setPasswordHash(encodedPassword);

        request = new EmployeeRequest();
        request.setIdentityNumber("A123456789");
        // request.setBirthDate(LocalDate.of(1990, 1, 1)); // 移除生日
        request.setName("張三");
        request.setIsActive(true);
        request.setPermissions(List.of("inventory_access"));
        request.setPassword(rawPassword);
    }

    @Test
    void testGetAllEmployees() {
        when(employeeRepository.findByTenantId(tenantId)).thenReturn(List.of(employee));
        List<Employee> employees = employeeService.getAllEmployees(tenantId);
        assertEquals(1, employees.size());
        assertEquals("張三", employees.get(0).getName());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeById(employeeId));
    }

    @Test
    void testCreateEmployee() {
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee created = employeeService.createEmployee(tenantId, request);

        assertEquals("張三", created.getName());
        assertEquals(tenantId, created.getTenantId());
        assertEquals(List.of("inventory_access"), created.getPermissions());
        assertEquals(encodedPassword, created.getPasswordHash());
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void testUpdateEmployee_WithPassword() {
        // 更新時提供新密碼
        request.setPassword("newPassword");
        String newEncoded = "bcrypt_hash_of_newPassword";
        when(passwordEncoder.encode("newPassword")).thenReturn(newEncoded);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee updated = employeeService.updateEmployee(employeeId, request);

        assertEquals("張三", updated.getName());
        assertEquals(newEncoded, updated.getPasswordHash());
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void testUpdateEmployee_WithoutPassword() {
        // 更新時不提供密碼，應保留原密碼哈希
        request.setPassword(null);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee updated = employeeService.updateEmployee(employeeId, request);

        assertEquals(encodedPassword, updated.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testDeleteEmployee() {
        doNothing().when(employeeRepository).deleteById(employeeId);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(employeeId));
        verify(employeeRepository).deleteById(employeeId);
    }

    @Test
    void testToggleActive() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        employeeService.toggleActive(employeeId, false);
        assertFalse(employee.getIsActive());
        verify(employeeRepository).save(employee);
    }
}