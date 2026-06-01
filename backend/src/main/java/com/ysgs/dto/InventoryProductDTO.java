package com.ysgs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InventoryProductDTO {
    private UUID id;
    private String name;
    private Integer safeQuantity;
    private Integer version;
    private String unit;
}