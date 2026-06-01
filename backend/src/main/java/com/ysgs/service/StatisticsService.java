package com.ysgs.service;

import com.ysgs.dto.ReplenishmentSummaryDTO;
import com.ysgs.repository.InvReplenishmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private InvReplenishmentRepository replenishmentRepository;

    public List<ReplenishmentSummaryDTO> getReplenishmentSummary(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        // 將 LocalDate 轉為 Instant（使用系統預設時區）
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        return replenishmentRepository.getReplenishmentSummary(tenantId, start, end);
    }
}