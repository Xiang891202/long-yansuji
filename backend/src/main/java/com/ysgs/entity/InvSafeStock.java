package com.ysgs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inv_safe_stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "day_of_week"})
})
@Data
public class InvSafeStock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;  // 1=Monday ... 7=Sunday

    @Column(name = "safe_quantity")
    private Integer safeQuantity = 0;

    @Version
    private Integer version;   // 樂觀鎖

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}