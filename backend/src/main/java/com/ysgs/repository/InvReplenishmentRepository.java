package com.ysgs.repository;

import com.ysgs.entity.InvReplenishment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvReplenishmentRepository extends JpaRepository<InvReplenishment, UUID> {
}