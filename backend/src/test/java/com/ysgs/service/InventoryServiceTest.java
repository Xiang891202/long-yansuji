package com.ysgs.service;

import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.entity.*;
import com.ysgs.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private final String employeeName = "測試員工";
    private final String idempotencyKey = "test-idem-key";

    private InvSafeStock safeStock;
    private InvProduct product;
    private InventoryReportRequest request;

    @BeforeEach
    void setUp() {
        // 建立共用測試資料
        UUID productId = UUID.randomUUID();
        safeStock = new InvSafeStock();
        safeStock.setProductId(productId);
        safeStock.setSafeQuantity(10);
        safeStock.setVersion(1);
        safeStock.setTenantId(tenantId);

        product = new InvProduct();
        product.setId(productId);
        product.setName("雞排");
        product.setUnit("片");
        product.setIsActive(true);

        InventoryReportRequest.InventoryItem item = new InventoryReportRequest.InventoryItem();
        item.setProductId(productId);
        item.setCurrentQuantity(3);
        item.setSafeStockVersion(1);

        request = new InventoryReportRequest();
        request.setDayOfWeek(1);
        request.setItems(List.of(item));
        request.setVegetables(List.of("高麗菜", "花椰菜"));
    }

    @Test
    void testCalculateReplenishment_IdempotencyHit() {
        // 模擬冪等記錄已存在
        IdempotencyRecord existing = new IdempotencyRecord();
        existing.setSupplierText("舊的叫貨文字");
        existing.setVegetableText("舊的蔬菜文字");
        when(idempotencyRepository.findById(idempotencyKey)).thenReturn(Optional.of(existing));

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, employeeName, request, idempotencyKey);

        assertEquals("舊的叫貨文字", result.getSupplierText());
        assertEquals("舊的蔬菜文字", result.getVegetableText());
        verify(safeStockRepository, never()).findByTenantIdAndDayOfWeek(any(), any());
    }

    @Test
    void testCalculateReplenishment_SuccessWithNeedOrder() {
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));
        when(productRepository.findById(safeStock.getProductId())).thenReturn(Optional.of(product));
        when(replenishmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(idempotencyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, employeeName, request, idempotencyKey);

        assertEquals("雞排：補 7 片\n", result.getSupplierText());
        assertEquals("高麗菜\n花椰菜", result.getVegetableText());

        // 驗證庫存回報儲存
        ArgumentCaptor<List<InvInventoryReport>> reportCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportRepository).saveAll(reportCaptor.capture());
        List<InvInventoryReport> reports = reportCaptor.getValue();
        assertEquals(1, reports.size());
        assertEquals(3, reports.get(0).getCurrentQuantity());

        // 驗證叫貨單儲存
        ArgumentCaptor<InvReplenishment> replenishmentCaptor = ArgumentCaptor.forClass(InvReplenishment.class);
        verify(replenishmentRepository).save(replenishmentCaptor.capture());
        InvReplenishment saved = replenishmentCaptor.getValue();
        assertEquals(employeeId, saved.getEmployeeId());
        assertEquals("draft", saved.getStatus());

        // 驗證叫貨明細儲存
        ArgumentCaptor<InvReplenishmentItem> itemCaptor = ArgumentCaptor.forClass(InvReplenishmentItem.class);
        verify(replenishmentItemRepository).save(itemCaptor.capture());
        InvReplenishmentItem ri = itemCaptor.getValue();
        assertEquals(7, ri.getQuantity());
        assertEquals("片", ri.getUnit());

        // 驗證冪等記錄儲存
        ArgumentCaptor<IdempotencyRecord> idemCaptor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotencyRepository).save(idemCaptor.capture());
        assertEquals(idempotencyKey, idemCaptor.getValue().getKey());
    }

    @Test
    void testCalculateReplenishment_NoNeedOrder() {
        request.getItems().get(0).setCurrentQuantity(10); // 庫存等於安全庫存
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));
        when(replenishmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(idempotencyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, employeeName, request, idempotencyKey);

        assertTrue(result.getSupplierText().isEmpty());
        verify(replenishmentItemRepository, never()).save(any());
    }

    @Test
    void testCalculateReplenishment_InvalidVersion() {
        request.getItems().get(0).setSafeStockVersion(99); // 版本不符
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                inventoryService.calculateReplenishment(tenantId, employeeId, employeeName, request, idempotencyKey));
        assertEquals("安全庫存已被更新，請重新整理頁面後再提交", exception.getMessage());
        verify(reportRepository, never()).saveAll(any());
    }

    @Test
    void testCalculateReplenishment_ProductNotFound() {
        when(safeStockRepository.findByTenantIdAndDayOfWeek(eq(tenantId), eq(1)))
                .thenReturn(List.of(safeStock));
        when(productRepository.findById(safeStock.getProductId())).thenReturn(Optional.empty());
        when(replenishmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(idempotencyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());

        ReplenishmentResult result = inventoryService.calculateReplenishment(
                tenantId, employeeId, employeeName, request, idempotencyKey);

        // 商品不存在時補貨清單應為空（不產生叫貨明細）
        assertTrue(result.getSupplierText().isEmpty());
        verify(replenishmentItemRepository, never()).save(any());
    }
}