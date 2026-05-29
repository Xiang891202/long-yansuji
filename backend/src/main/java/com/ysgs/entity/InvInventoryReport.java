package com.ysgs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inv_inventory_reports")
@Data
public class InvInventoryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "current_quantity")
    private Integer currentQuantity;

    @Column(name = "reported_by")
    private String reportedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}