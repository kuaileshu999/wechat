package com.studyroom.repository;

import com.studyroom.entity.ConsumptionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {
    Page<ConsumptionRecord> findByCampusIdInAndStatus(List<Long> campusIds, String status, Pageable pageable);

    @Query("SELECT cr FROM ConsumptionRecord cr JOIN Student s ON s.id = cr.studentId "
            + "WHERE cr.campusId IN :campusIds AND cr.status = :status "
            + "AND (:keyword IS NULL OR :keyword = '' OR s.name LIKE CONCAT('%', :keyword, '%') "
            + "OR s.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<ConsumptionRecord> searchCompleted(@Param("campusIds") List<Long> campusIds,
                                            @Param("status") String status,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    List<ConsumptionRecord> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
