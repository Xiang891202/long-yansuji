package com.ysgs.service;

import com.ysgs.entity.InvSafeStock;
import com.ysgs.repository.InvSafeStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class InvSafeStockService {

    @Autowired
    private InvSafeStockRepository safeStockRepository;

    public List<InvSafeStock> getSafeStocksByDay(Integer tenantId, Integer dayOfWeek) {
        return safeStockRepository.findByTenantIdAndDayOfWeek(tenantId, dayOfWeek);
    }

    @Transactional
    public InvSafeStock saveSafeStock(InvSafeStock stock) {
        return safeStockRepository.save(stock);
    }

    @Transactional
    public void updateSafeStockWithVersion(InvSafeStock stock) {
        try {
            safeStockRepository.save(stock);
        } catch (OptimisticLockingFailureException e) {
            throw new RuntimeException("安全庫存已被他人修改，請重新整理後再試", e);
        }
    }
}