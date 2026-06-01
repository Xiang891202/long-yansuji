package com.ysgs.controller;

import com.ysgs.config.TenantContext;
import com.ysgs.dto.ReplenishmentSummaryDTO;
import com.ysgs.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
@PreAuthorize("hasAuthority('admin')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/replenishment")
    public List<ReplenishmentSummaryDTO> getReplenishmentSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // System.out.println("=== StatisticsController.getReplenishmentSummary called ===");
        Integer tenantId = TenantContext.getTenantId();
        return statisticsService.getReplenishmentSummary(tenantId, startDate, endDate);
    }
}