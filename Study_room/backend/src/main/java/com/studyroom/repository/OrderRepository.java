package com.studyroom.repository;

import com.studyroom.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCampusIdIn(List<Long> campusIds, Pageable pageable);
    Optional<Order> findByOrderNo(String orderNo);

    @Query("SELECT o FROM Order o WHERE o.campusId IN :campusIds AND (:campusId IS NULL OR o.campusId = :campusId) " +
           "AND (:startDate IS NULL OR o.paymentDate >= :startDate) AND (:endDate IS NULL OR o.paymentDate <= :endDate)")
    Page<Order> search(@Param("campusIds") List<Long> campusIds, @Param("campusId") Long campusId,
                       @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o "
            + "WHERE o.studentId = :studentId AND o.campusId = :campusId "
            + "AND o.status <> com.studyroom.enums.OrderStatus.REFUNDED "
            + "AND (o.paidAmount - o.refundedAmount - o.consumedAmount) > 0")
    boolean hasPendingAmountOrder(@Param("studentId") Long studentId, @Param("campusId") Long campusId);
}
