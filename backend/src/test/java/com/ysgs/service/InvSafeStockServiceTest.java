package com.ysgs.service;

import com.ysgs.entity.InvSafeStock;
import com.ysgs.repository.InvSafeStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvSafeStockServiceTest {

    @Mock private InvSafeStockRepository safeStockRepository;

    @InjectMocks
    private InvSafeStockService safeStockService;

    private InvSafeStock stock;
    private final Integer tenantId = 2;
    private final UUID productId = UUID.randomUUID();
    private final Integer dayOfWeek = 1;

    @BeforeEach
    void setUp() {
        stock = new InvSafeStock();
        stock.setTenantId(tenantId);
        stock.setProductId(productId);
        stock.setDayOfWeek(dayOfWeek);
        stock.setSafeQuantity(10);
        stock.setVersion(0);
    }

    @Test
    void testSaveSafeStock_NewRecord() {
        when(safeStockRepository.findByTenantIdAndProductIdAndDayOfWeek(tenantId, productId, dayOfWeek))
                .thenReturn(Optional.empty());
        when(safeStockRepository.save(any(InvSafeStock.class))).thenAnswer(i -> i.getArgument(0));

        InvSafeStock saved = safeStockService.saveSafeStock(stock);

        assertNull(saved.getId()); // 因為 save 會產生新 ID，此處模擬返回同物件未設定 ID
        assertEquals(0, saved.getVersion());
        verify(safeStockRepository).save(stock);
    }

    @Test
    void testSaveSafeStock_ExistingRecord_Update() {
        InvSafeStock existing = new InvSafeStock();
        existing.setId(UUID.randomUUID());
        existing.setTenantId(tenantId);
        existing.setProductId(productId);
        existing.setDayOfWeek(dayOfWeek);
        existing.setSafeQuantity(5);
        existing.setVersion(1);

        when(safeStockRepository.findByTenantIdAndProductIdAndDayOfWeek(tenantId, productId, dayOfWeek))
                .thenReturn(Optional.of(existing));
        when(safeStockRepository.save(any(InvSafeStock.class))).thenAnswer(i -> i.getArgument(0));

        stock.setSafeQuantity(20);
        InvSafeStock saved = safeStockService.saveSafeStock(stock);

        assertEquals(20, saved.getSafeQuantity());
        assertEquals(1, saved.getVersion()); // 版本應保持不變（由 @Version 自動增加，此處模擬未變）
        verify(safeStockRepository).save(existing);
    }

    @Test
    void testUpdateSafeStockWithVersion_OptimisticLockFailure() {
        when(safeStockRepository.save(any(InvSafeStock.class)))
                .thenThrow(new OptimisticLockingFailureException("樂觀鎖失敗"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> safeStockService.updateSafeStockWithVersion(stock));
        assertTrue(exception.getMessage().contains("安全庫存已被他人修改"));
    }
}