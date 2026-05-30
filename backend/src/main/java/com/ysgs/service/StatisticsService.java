package com.ysgs.service;

import com.ysgs.dto.ReplenishmentSummaryDTO;
import com.ysgs.repository.InvReplenishmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private InvReplenishmentRepository replenishmentRepository;

    public List<ReplenishmentSummaryDTO> getReplenishmentSummary(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return replenishmentRepository.getReplenishmentSummary(tenantId, start, end);
    }
}