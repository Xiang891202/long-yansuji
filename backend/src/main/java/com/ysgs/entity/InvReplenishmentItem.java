package com.ysgs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "inv_replenishment_items")
@Data
public class InvReplenishmentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "replenishment_id")
    private UUID replenishmentId;

    @Column(name = "product_id")
    private UUID productId;

    private Integer quantity;

    private String unit;
}