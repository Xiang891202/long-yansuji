package com.ysgs.repository;

import com.ysgs.entity.InvSafeStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvSafeStockRepository extends JpaRepository<InvSafeStock, UUID> {
    List<InvSafeStock> findByTenantIdAndDayOfWeek(Integer tenantId, Integer dayOfWeek);
    Optional<InvSafeStock> findByTenantIdAndProductIdAndDayOfWeek(Integer tenantId, UUID productId, Integer dayOfWeek);
}