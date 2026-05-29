package com.ysgs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "idempotency_records")
@Data
public class IdempotencyRecord {
    @Id
    private String key;

    @Column(name = "supplier_text", columnDefinition = "TEXT")
    private String supplierText;

    @Column(name = "vegetable_text", columnDefinition = "TEXT")
    private String vegetableText;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}