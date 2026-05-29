package com.ysgs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inv_replenishments")
@Data
public class InvReplenishment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "safe_stock_version")
    private Integer safeStockVersion;

    @Column(length = 20)
    private String status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}