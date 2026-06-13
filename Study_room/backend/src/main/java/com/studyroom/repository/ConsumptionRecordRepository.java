package com.studyroom.repository;

import com.studyroom.entity.ConsumptionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {
    Page<ConsumptionRecord> findByCampusIdInAndStatus(List<Long> campusIds, String status, Pageable pageable);
    List<ConsumptionRecord> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
