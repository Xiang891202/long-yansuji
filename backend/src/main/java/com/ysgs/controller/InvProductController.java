package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.entity.InvProduct;
import com.ysgs.service.InvProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasAuthority('admin')")
public class InvProductController {

    @Autowired
    private InvProductService productService;

    @GetMapping
    public List<InvProduct> getAllProducts(
            @RequestParam(required = false) Boolean onlyActive) {
        Integer tenantId = TenantContext.getTenantId();
        return productService.getAllProducts(tenantId, onlyActive);
    }

    @GetMapping("/category/{categoryId}")
    public List<InvProduct> getProductsByCategory(@PathVariable UUID categoryId) {
        Integer tenantId = TenantContext.getTenantId();
        return productService.getProductsByCategory(tenantId, categoryId);
    }

    @GetMapping("/{id}")
    public InvProduct getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public ResponseEntity<InvProduct> createProduct(@RequestBody InvProduct product) {
        product.setTenantId(TenantContext.getTenantId());
        InvProduct saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public InvProduct updateProduct(@PathVariable UUID id, @RequestBody InvProduct product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id, @RequestParam Boolean isActive) {
        productService.toggleActive(id, isActive);
        return ResponseEntity.ok().build();
    }
}