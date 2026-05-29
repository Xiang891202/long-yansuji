package com.ysgs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceLabel {
    private String label;  // e.g., "小份"
    private Integer price; // e.g., 60
}