package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.dto.InventoryProductDTO;
import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.entity.InvProduct;
import com.ysgs.entity.InvSafeStock;
import com.ysgs.repository.InvProductRepository;
import com.ysgs.repository.InvSafeStockRepository;
import com.ysgs.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ysgs.dto.InventoryProductDTO;

import java.security.Principal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.*;

@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasAuthority('inventory_access')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InvSafeStockRepository safeStockRepository;

    @Autowired
    private InvProductRepository productRepository;

    @PostMapping("/calculate")
    public ResponseEntity<ReplenishmentResult> calculate(
            @RequestBody InventoryReportRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Principal principal) {

        Integer tenantId = TenantContext.getTenantId();
        UUID employeeId = UUID.fromString(principal.getName()); // 假設 principal name 是 employeeId
        String employeeName = principal.getName(); // 可改為查詢員工姓名

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, employeeName, request, idempotencyKey);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('inventory_access')")
    public List<InventoryProductDTO> getProductsForInventory(@RequestParam Integer dayOfWeek) {
        Integer tenantId = TenantContext.getTenantId();
        // 查詢該星期有安全庫存的記錄
        List<InvSafeStock> safeStocks = safeStockRepository.findByTenantIdAndDayOfWeek(tenantId, dayOfWeek);
        if (safeStocks.isEmpty()) {
            return Collections.emptyList();
        }
        // 取得對應的商品資料（僅上架中的）
        Set<UUID> productIds = safeStocks.stream().map(InvSafeStock::getProductId).collect(Collectors.toSet());
        List<InvProduct> products = productRepository.findAllById(productIds);
        Map<UUID, InvProduct> productMap = products.stream()
                .filter(InvProduct::getIsActive)
                .collect(Collectors.toMap(InvProduct::getId, p -> p));
        
        List<InventoryProductDTO> result = new ArrayList<>();
        for (InvSafeStock stock : safeStocks) {
            InvProduct product = productMap.get(stock.getProductId());
            if (product != null) {
                result.add(new InventoryProductDTO(
                    product.getId(),
                    product.getName(),
                    stock.getSafeQuantity(),
                    stock.getVersion(),
                    product.getUnit()
                ));
            }
        }
        return result;
    }
}