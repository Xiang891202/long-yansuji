package com.ysgs.repository;

import com.ysgs.entity.InvInventoryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvInventoryReportRepository extends JpaRepository<InvInventoryReport, UUID> {
}