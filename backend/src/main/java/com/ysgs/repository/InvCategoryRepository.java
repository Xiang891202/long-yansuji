package com.ysgs.repository;

import com.ysgs.entity.InvCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvCategoryRepository extends JpaRepository<InvCategory, UUID> {
    List<InvCategory> findByTenantIdOrderBySortOrderAsc(Integer tenantId);
    Optional<InvCategory> findByTenantIdAndCode(Integer tenantId, String code);
}