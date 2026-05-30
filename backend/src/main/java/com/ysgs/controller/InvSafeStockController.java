package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.entity.InvSafeStock;
import com.ysgs.service.InvSafeStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/safe-stocks")
@PreAuthorize("hasAuthority('admin')")
public class InvSafeStockController {

    @Autowired
    private InvSafeStockService safeStockService;

    @GetMapping
    public List<InvSafeStock> getByDay(@RequestParam Integer dayOfWeek) {
        Integer tenantId = TenantContext.getTenantId();
        return safeStockService.getSafeStocksByDay(tenantId, dayOfWeek);
    }

    @PutMapping
    public InvSafeStock updateSafeStock(@RequestBody InvSafeStock stock) {
        Integer tenantId = TenantContext.getTenantId();
        System.out.println("Current tenantId: " + tenantId);
        stock.setTenantId(tenantId);
        return safeStockService.saveSafeStock(stock);
    }
}