package com.studyroom.repository;

import com.studyroom.entity.OrderSalesperson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderSalespersonRepository extends JpaRepository<OrderSalesperson, Long> {

    List<OrderSalesperson> findByOrderId(Long orderId);

    List<OrderSalesperson> findByOrderIdIn(Collection<Long> orderIds);

    void deleteByOrderId(Long orderId);
}
