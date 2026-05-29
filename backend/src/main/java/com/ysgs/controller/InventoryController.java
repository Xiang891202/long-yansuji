package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasAuthority('inventory_access')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

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
}