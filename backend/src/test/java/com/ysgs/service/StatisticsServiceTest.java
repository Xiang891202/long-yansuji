package com.ysgs.service;

import com.ysgs.dto.ReplenishmentSummaryDTO;
import com.ysgs.repository.InvReplenishmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private InvReplenishmentRepository replenishmentRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void testGetReplenishmentSummary() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        List<ReplenishmentSummaryDTO> mockData = List.of(
                new ReplenishmentSummaryDTO(UUID.randomUUID(), "雞排", 120L, 5L),
                new ReplenishmentSummaryDTO(UUID.randomUUID(), "甜不辣", 80L, 3L)
        );
        when(replenishmentRepository.getReplenishmentSummary(eq(2), any(), any()))
                .thenReturn(mockData);

        List<ReplenishmentSummaryDTO> result = statisticsService.getReplenishmentSummary(2, start, end);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("雞排", result.get(0).getProductName());
        verify(replenishmentRepository).getReplenishmentSummary(eq(2),
                argThat(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate().equals(start)),
                argThat(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate().equals(end)));
    }
}