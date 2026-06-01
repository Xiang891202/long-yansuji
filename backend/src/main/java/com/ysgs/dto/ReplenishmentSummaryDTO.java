package com.ysgs.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReplenishmentSummaryDTO {
    private UUID productId;
    private String productName;
    private Long totalQuantity;   // 改為 Long
    private Long count;           // 改為 Long
}