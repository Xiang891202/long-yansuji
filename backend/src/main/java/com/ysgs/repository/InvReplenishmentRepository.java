package com.ysgs.repository;

import com.ysgs.dto.ReplenishmentSummaryDTO;
import com.ysgs.entity.InvReplenishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InvReplenishmentRepository extends JpaRepository<InvReplenishment, UUID> {

    @Query("SELECT new com.ysgs.dto.ReplenishmentSummaryDTO(ri.productId, p.name, SUM(ri.quantity), COUNT(ri)) " +
       "FROM InvReplenishmentItem ri " +
       "JOIN InvReplenishment r ON ri.replenishmentId = r.id " +
       "JOIN InvProduct p ON ri.productId = p.id " +
       "WHERE r.tenantId = :tenantId AND r.createdAt BETWEEN :startDate AND :endDate " +
       "GROUP BY ri.productId, p.name")
       List<ReplenishmentSummaryDTO> getReplenishmentSummary(@Param("tenantId") Integer tenantId,
                                                        @Param("startDate") Instant startDate,
                                                        @Param("endDate") Instant endDate);
}