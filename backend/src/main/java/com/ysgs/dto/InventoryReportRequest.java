package com.ysgs.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class InventoryReportRequest {
    private Integer dayOfWeek;
    private List<InventoryItem> items;
    private List<String> vegetables;

    @Data
    public static class InventoryItem {
        private UUID productId;
        private Integer currentQuantity;
        private Integer safeStockVersion;
    }
}