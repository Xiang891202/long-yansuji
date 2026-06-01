package com.ysgs.controller;

import com.ysgs.dto.ProductPublicDTO;
import com.ysgs.entity.InvCategory;
import com.ysgs.entity.InvProduct;
import com.ysgs.repository.InvCategoryRepository;
import com.ysgs.repository.InvProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private InvProductRepository productRepository;

    @Autowired
    private InvCategoryRepository categoryRepository;   // 加入這個注入

    @GetMapping("/products")
    public List<ProductPublicDTO> getProducts(@RequestParam(required = false) String categoryId) {
        List<InvProduct> products;
        if (categoryId != null && !categoryId.isEmpty()) {
            products = productRepository.findByTenantIdAndCategoryIdAndIsActiveTrueOrderBySortOrderAsc(2, UUID.fromString(categoryId));
        } else {
            products = productRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(2);
        }
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<InvCategory> getCategories() {
        return categoryRepository.findByTenantIdOrderBySortOrderAsc(2);
    }

    @GetMapping("/vegetables")
    public List<ProductPublicDTO> getVegetables() {
        // 租戶 ID 固定為 2（鹽酥雞），從資料庫找出青菜類分類
        InvCategory vegCategory = categoryRepository.findByTenantIdAndCode(2, "vegetable")
                .orElseThrow(() -> new RuntimeException("青菜分類不存在，請先建立分類 code=vegetable"));
        List<InvProduct> products = productRepository.findByTenantIdAndCategoryIdAndIsActiveTrueOrderBySortOrderAsc(2, vegCategory.getId());
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ProductPublicDTO convertToDTO(InvProduct product) {
        ProductPublicDTO dto = new ProductPublicDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrices(product.getPrices());
        dto.setUnit(product.getUnit());
        return dto;
    }
}