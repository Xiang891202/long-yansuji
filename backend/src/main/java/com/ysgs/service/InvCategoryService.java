package com.ysgs.service;

import com.ysgs.entity.InvCategory;
import com.ysgs.repository.InvCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;   // 這是缺失的導入

@Service
public class InvCategoryService {

    @Autowired
    private InvCategoryRepository categoryRepository;

    public List<InvCategory> getAllCategories(Integer tenantId) {
        return categoryRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
    }

    public InvCategory getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("分類不存在"));
    }

    @Transactional
    public InvCategory createCategory(InvCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public InvCategory updateCategory(UUID id, InvCategory categoryDetails) {
        InvCategory category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setCode(categoryDetails.getCode());
        category.setSortOrder(categoryDetails.getSortOrder());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }
}