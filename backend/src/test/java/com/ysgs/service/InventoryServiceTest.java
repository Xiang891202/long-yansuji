package com.ysgs.service;

import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.entity.*;
import com.ysgs.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InvSafeStockRepository safeStockRepository;
    @Mock private InvProductRepository productRepository;
    @Mock private InvInventoryReportRepository reportRepository;
    @Mock private InvReplenishmentRepository replenishmentRepository;
    @Mock private InvReplenishmentItemRepository replenishmentItemRepository;
    @Mock private IdempotencyRecordRepository idempotencyRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private final Integer tenantId = 2;
    private final UUID employeeId = UUID.randomUUID();
    private final String idempotencyKey = "test-key";

    @BeforeEach
    void setup() {
        when(idempotencyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());
    }

    @Test
    void testCalculateReplenishment_NoChange() {
        UUID productId = UUID.randomUUID();
        InvSafeStock safeStock = new InvSafeStock();
        safeStock.setProductId(productId);
        safeStock.setSafeQuantity(10);
        safeStock.setVersion(1);
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));

        InvProduct product = new InvProduct();
        product.setId(productId);
        product.setName("雞排");
        product.setUnit("包");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        InventoryReportRequest request = new InventoryReportRequest();
        request.setDayOfWeek(1);
        InventoryReportRequest.InventoryItem item = new InventoryReportRequest.InventoryItem();
        item.setProductId(productId);
        item.setCurrentQuantity(10);
        item.setSafeStockVersion(1);
        request.setItems(List.of(item));
        request.setVegetables(List.of("高麗菜"));

        when(replenishmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, "tester", request, idempotencyKey);

        assertTrue(result.getSupplierText().isEmpty());
        assertEquals("高麗菜", result.getVegetableText());
        verify(reportRepository, times(1)).saveAll(any());
        verify(replenishmentItemRepository, never()).save(any());
        verify(idempotencyRepository).save(any());
    }

    @Test
    void testCalculateReplenishment_NeedOrder() {
        UUID productId = UUID.randomUUID();
        InvSafeStock safeStock = new InvSafeStock();
        safeStock.setProductId(productId);
        safeStock.setSafeQuantity(10);
        safeStock.setVersion(1);
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));

        InvProduct product = new InvProduct();
        product.setId(productId);
        product.setName("雞排");
        product.setUnit("包");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        InventoryReportRequest request = new InventoryReportRequest();
        request.setDayOfWeek(1);
        InventoryReportRequest.InventoryItem item = new InventoryReportRequest.InventoryItem();
        item.setProductId(productId);
        item.setCurrentQuantity(3);
        item.setSafeStockVersion(1);
        request.setItems(List.of(item));
        request.setVegetables(List.of("高麗菜"));

        when(replenishmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, "tester", request, idempotencyKey);

        assertEquals("雞排：補 7 包\n", result.getSupplierText());
        verify(replenishmentItemRepository, times(1)).save(any());
    }

    @Test
    void testIdempotency() {
        String key = "dup-key";
        IdempotencyRecord existing = new IdempotencyRecord();
        existing.setSupplierText("cached text");
        existing.setVegetableText("cached veg");
        when(idempotencyRepository.findById(key)).thenReturn(Optional.of(existing));

        InventoryReportRequest request = new InventoryReportRequest();
        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, "tester", request, key);

        assertEquals("cached text", result.getSupplierText());
        verify(safeStockRepository, never()).findByTenantIdAndDayOfWeek(any(), any());
    }
}