package com.ysgs.repository;

import com.ysgs.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByTenantIdAndEmailAndPhone(Integer tenantId, String email, String phone);
}