package com.studyroom.repository;

import com.studyroom.entity.OrderRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRefundRepository extends JpaRepository<OrderRefund, Long> {
    List<OrderRefund> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
