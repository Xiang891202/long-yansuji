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

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequest request;
    private final Integer tenantId = 2;
    private final UUID employeeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);
        employee.setTenantId(tenantId);
        employee.setIdentityNumber("A123456789");
        employee.setBirthDate(LocalDate.of(1990, 1, 1));
        employee.setName("張三");
        employee.setIsActive(true);
        employee.setPermissions(List.of("inventory_access"));

        request = new EmployeeRequest();
        request.setIdentityNumber("A123456789");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setName("張三");
        request.setIsActive(true);
        request.setPermissions(List.of("inventory_access"));
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
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee created = employeeService.createEmployee(tenantId, request);
        assertEquals("張三", created.getName());
        assertEquals(tenantId, created.getTenantId());
        assertEquals(List.of("inventory_access"), created.getPermissions());
    }

    @Test
    void testUpdateEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        request.setName("李四");
        Employee updated = employeeService.updateEmployee(employeeId, request);
        assertEquals("李四", updated.getName());
        verify(employeeRepository).save(employee);
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