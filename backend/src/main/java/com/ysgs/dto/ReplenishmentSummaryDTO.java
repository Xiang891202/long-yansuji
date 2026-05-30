package com.ysgs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReplenishmentSummaryDTO {
    private UUID productId;
    private String productName;
    private Integer totalQuantity;
    private Integer count;
}