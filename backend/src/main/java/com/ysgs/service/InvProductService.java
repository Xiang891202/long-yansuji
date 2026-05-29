package com.ysgs.service;

import com.ysgs.entity.InvProduct;
import com.ysgs.repository.InvProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class InvProductService {

    @Autowired
    private InvProductRepository productRepository;

    public List<InvProduct> getAllProducts(Integer tenantId, Boolean onlyActive) {
        if (onlyActive != null && onlyActive) {
            return productRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId);
        }
        return productRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
    }

    public List<InvProduct> getProductsByCategory(Integer tenantId, UUID categoryId) {
        return productRepository.findByTenantIdAndCategoryId(tenantId, categoryId);
    }

    public InvProduct getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Transactional
    public InvProduct createProduct(InvProduct product) {
        return productRepository.save(product);
    }

    @Transactional
    public InvProduct updateProduct(UUID id, InvProduct productDetails) {
        InvProduct product = getProductById(id);
        product.setCategoryId(productDetails.getCategoryId());
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setPrices(productDetails.getPrices());
        product.setUnit(productDetails.getUnit());
        product.setIsActive(productDetails.getIsActive());
        product.setSortOrder(productDetails.getSortOrder());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void toggleActive(UUID id, Boolean isActive) {
        InvProduct product = getProductById(id);
        product.setIsActive(isActive);
        productRepository.save(product);
    }
}