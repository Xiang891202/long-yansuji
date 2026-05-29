package com.ysgs.repository;

import com.ysgs.entity.InvProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvProductRepository extends JpaRepository<InvProduct, UUID> {
    List<InvProduct> findByTenantIdOrderBySortOrderAsc(Integer tenantId);
    List<InvProduct> findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(Integer tenantId);
    List<InvProduct> findByTenantIdAndCategoryId(Integer tenantId, UUID categoryId);
}