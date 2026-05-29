package com.ysgs.repository;

import com.ysgs.entity.InvReplenishmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvReplenishmentItemRepository extends JpaRepository<InvReplenishmentItem, UUID> {
}