package com.ysgs.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProductPublicDTO {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private List<PriceLabel> prices;
    private String unit;
}