package com.ysgs.controller;
import java.util.UUID;


import com.ysgs.config.TenantContext;
import com.ysgs.entity.InvCategory;
import com.ysgs.service.InvCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@PreAuthorize("hasAuthority('admin')") // 只有管理員可存取
public class InvCategoryController {

    @Autowired
    private InvCategoryService categoryService;

    @GetMapping
    public List<InvCategory> getAllCategories() {
        Integer tenantId = TenantContext.getTenantId();
        return categoryService.getAllCategories(tenantId);
    }

    @GetMapping("/{id}")
    public InvCategory getCategoryById(@PathVariable UUID id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    public ResponseEntity<InvCategory> createCategory(@RequestBody InvCategory category) {
        category.setTenantId(TenantContext.getTenantId());
        InvCategory saved = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public InvCategory updateCategory(@PathVariable UUID id, @RequestBody InvCategory category) {
        return categoryService.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}