package com.studyroom.repository;

import com.studyroom.entity.OrderTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderTeacherRepository extends JpaRepository<OrderTeacher, Long> {

    List<OrderTeacher> findByOrderId(Long orderId);

    List<OrderTeacher> findByOrderIdIn(Collection<Long> orderIds);

    void deleteByOrderId(Long orderId);
}
