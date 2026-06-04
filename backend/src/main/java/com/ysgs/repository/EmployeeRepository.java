package com.ysgs.repository;

import com.ysgs.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;          // 補上這一行
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByTenantIdAndIdentityNumberAndBirthDate(Integer tenantId, String identityNumber, LocalDate birthDate);
    List<Employee> findByTenantId(Integer tenantId);
    Optional<Employee> findByIdentityNumber(String identityNumber);   // 新增
}