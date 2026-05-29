package com.ysgs.service;

import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.entity.*;
import com.ysgs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import com.ysgs.repository.InvReplenishmentRepository;
import com.ysgs.repository.InvReplenishmentItemRepository;

@Service
public class InventoryService {

    @Autowired
    private InvSafeStockRepository safeStockRepository;

    @Autowired
    private InvProductRepository productRepository;

    @Autowired
    private InvInventoryReportRepository reportRepository;

    @Autowired
    private InvReplenishmentRepository replenishmentRepository;

    @Autowired
    private InvReplenishmentItemRepository replenishmentItemRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRepository;

    @Transactional
    public ReplenishmentResult calculateReplenishment(
            Integer tenantId,
            UUID employeeId,
            String employeeName,
            InventoryReportRequest request,
            String idempotencyKey) {

        // 1. 冪等性檢查
        Optional<IdempotencyRecord> existing = idempotencyRepository.findById(idempotencyKey);
        if (existing.isPresent()) {
            return new ReplenishmentResult(existing.get().getSupplierText(), existing.get().getVegetableText());
        }

        // 2. 載入安全庫存（含產品資訊）
        List<InvSafeStock> safeStocks = safeStockRepository.findByTenantIdAndDayOfWeek(tenantId, request.getDayOfWeek());
        Map<UUID, InvSafeStock> safeMap = safeStocks.stream()
                .collect(Collectors.toMap(InvSafeStock::getProductId, s -> s));

        // 3. 檢查樂觀鎖版本
        for (InventoryReportRequest.InventoryItem item : request.getItems()) {
            InvSafeStock safe = safeMap.get(item.getProductId());
            if (safe == null || !safe.getVersion().equals(item.getSafeStockVersion())) {
                throw new RuntimeException("安全庫存已被更新，請重新整理頁面後再提交");
            }
        }

        // 4. 儲存庫存回報記錄
        List<InvInventoryReport> reports = new ArrayList<>();
        for (InventoryReportRequest.InventoryItem item : request.getItems()) {
            InvInventoryReport report = new InvInventoryReport();
            report.setTenantId(tenantId);
            report.setProductId(item.getProductId());
            report.setEmployeeId(employeeId);
            report.setDayOfWeek(request.getDayOfWeek());
            report.setCurrentQuantity(item.getCurrentQuantity());
            report.setReportedBy(employeeName);
            reports.add(report);
        }
        reportRepository.saveAll(reports);

        // 5. 計算補貨清單
        List<InvReplenishmentItem> replenishmentItems = new ArrayList<>();
        StringBuilder supplierText = new StringBuilder();
        for (InventoryReportRequest.InventoryItem item : request.getItems()) {
            InvSafeStock safe = safeMap.get(item.getProductId());
            int need = Math.max(0, safe.getSafeQuantity() - item.getCurrentQuantity());
            if (need > 0) {
                InvProduct product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    String line = String.format("%s：補 %d %s", product.getName(), need, product.getUnit());
                    supplierText.append(line).append("\n");

                    InvReplenishmentItem ri = new InvReplenishmentItem();
                    ri.setTenantId(tenantId);
                    ri.setProductId(item.getProductId());
                    ri.setQuantity(need);
                    ri.setUnit(product.getUnit());
                    replenishmentItems.add(ri);
                }
            }
        }

        // 6. 儲存叫貨單
        InvReplenishment replenishment = new InvReplenishment();
        replenishment.setTenantId(tenantId);
        replenishment.setEmployeeId(employeeId);
        replenishment.setSafeStockVersion(safeStocks.isEmpty() ? 1 : safeStocks.get(0).getVersion());
        replenishment.setStatus("draft");
        replenishment.setCreatedBy(employeeName);
        replenishment = replenishmentRepository.save(replenishment);

        for (InvReplenishmentItem ri : replenishmentItems) {
            ri.setReplenishmentId(replenishment.getId());
            replenishmentItemRepository.save(ri);
        }

        // 7. 蔬菜文字
        String vegetableText = String.join("\n", request.getVegetables());

        // 8. 儲存冪等記錄
        IdempotencyRecord record = new IdempotencyRecord();
        record.setKey(idempotencyKey);
        record.setSupplierText(supplierText.toString());
        record.setVegetableText(vegetableText);
        idempotencyRepository.save(record);

        return new ReplenishmentResult(supplierText.toString(), vegetableText);
    }
}