package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.OrderCreateRequest;
import com.studyroom.dto.OrderDetailVO;
import com.studyroom.dto.OrderUpdateRequest;
import com.studyroom.dto.RefundRequest;
import com.studyroom.entity.*;
import com.studyroom.enums.OrderStatus;
import com.studyroom.repository.*;
import com.studyroom.security.SecurityUtils;
import com.studyroom.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderRefundRepository refundRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CampusRepository campusRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final CampusService campusService;

    public PageResult<Order> list(Long campusId, LocalDate startDate, LocalDate endDate, int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
        }
        Page<Order> result = orderRepository.search(campusIds, campusId, startDate, endDate,
                PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public OrderDetailVO getDetail(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());

        Student student = studentRepository.findById(order.getStudentId()).orElse(null);
        Course course = courseRepository.findById(order.getCourseId()).orElse(null);
        Campus campus = campusRepository.findById(order.getCampusId()).orElse(null);
        Employee salesperson = employeeRepository.findById(order.getSalespersonId()).orElse(null);

        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount()).max(BigDecimal.ZERO);
        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours())
                .subtract(order.getConsumedHours()).max(BigDecimal.ZERO);

        return OrderDetailVO.builder()
                .order(order)
                .studentName(student != null ? student.getName() : "")
                .studentPhone(student != null ? student.getPhone() : "")
                .courseName(course != null ? course.getName() : "")
                .campusName(campus != null ? campus.getName() : "")
                .salespersonName(salesperson != null ? salesperson.getName() : "")
                .pendingAmount(pendingAmount)
                .pendingHours(pendingHours)
                .refunds(refundRepository.findByOrderIdOrderByCreatedAtDesc(id))
                .auditLogs(auditLogService.getLogs("Order", id))
                .build();
    }

    @Transactional
    public Order create(OrderCreateRequest request) {
        campusService.ensureCampusEnabled(request.getCampusId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("学员不存在"));
        if (!student.getCampusId().equals(request.getCampusId())) {
            throw new BusinessException("学员不属于该校区");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));
        if (!course.getCampusId().equals(request.getCampusId()) || course.getStatus() != 1) {
            throw new BusinessException("课程不可用");
        }

        if (request.getPaidAmount().stripTrailingZeros().scale() > 0
                && request.getPaidAmount().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("收款金额必须为正整数");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setCampusId(request.getCampusId());
        order.setStudentId(request.getStudentId());
        order.setCourseId(request.getCourseId());
        order.setTotalHours(request.getTotalHours());
        order.setPaidAmount(request.getPaidAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentDate(request.getPaymentDate());
        order.setSalespersonId(request.getSalespersonId());
        order.setRemark(request.getRemark());
        order.setCreatedBy(SecurityUtils.getCurrentUserId());

        Order saved = orderRepository.save(order);
        auditLogService.log("Order", saved.getId(), "CREATE",
                "新建订单 " + saved.getOrderNo() + ", 金额 " + saved.getPaidAmount());
        return saved;
    }

    @Transactional
    public Order update(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("已全额退费的订单不能编辑");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("学员不存在"));
        if (!student.getCampusId().equals(order.getCampusId())) {
            throw new BusinessException("学员不属于该校区");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));
        if (!course.getCampusId().equals(order.getCampusId()) || course.getStatus() != 1) {
            throw new BusinessException("课程不可用");
        }

        if (request.getPaidAmount().stripTrailingZeros().scale() > 0
                && request.getPaidAmount().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("收款金额必须为正整数");
        }
        if (BigDecimal.valueOf(request.getTotalHours()).compareTo(order.getConsumedHours()) < 0) {
            throw new BusinessException("课时数不能小于已消课时 " + order.getConsumedHours());
        }
        BigDecimal minPaid = order.getConsumedAmount().add(order.getRefundedAmount());
        if (request.getPaidAmount().compareTo(minPaid) < 0) {
            throw new BusinessException("收款金额不能小于已消课与已退费合计 " + minPaid);
        }

        order.setStudentId(request.getStudentId());
        order.setCourseId(request.getCourseId());
        order.setTotalHours(request.getTotalHours());
        order.setPaidAmount(request.getPaidAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentDate(request.getPaymentDate());
        order.setSalespersonId(request.getSalespersonId());
        order.setRemark(request.getRemark());

        Order saved = orderRepository.save(order);
        auditLogService.log("Order", saved.getId(), "UPDATE", "编辑订单 " + saved.getOrderNo());
        return saved;
    }

    @Transactional
    public OrderRefund refund(Long orderId, RefundRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());

        BigDecimal maxRefund = order.getPaidAmount().subtract(order.getConsumedAmount())
                .subtract(order.getRefundedAmount());
        if (maxRefund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("当前无可退金额");
        }
        if (request.getRefundAmount().compareTo(maxRefund) > 0) {
            throw new BusinessException("退费金额不能超过可退金额 " + maxRefund);
        }

        OrderRefund refund = new OrderRefund();
        refund.setOrderId(orderId);
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundReason(request.getRefundReason());
        refund.setRefundMethod(request.getRefundMethod());
        refund.setRemark(request.getRemark());
        refund.setCreatedBy(SecurityUtils.getCurrentUserId());
        refundRepository.save(refund);

        order.setRefundedAmount(order.getRefundedAmount().add(request.getRefundAmount()));
        BigDecimal remaining = order.getPaidAmount().subtract(order.getRefundedAmount());
        if (remaining.compareTo(order.getConsumedAmount()) <= 0 && remaining.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.REFUNDED);
        } else if (order.getRefundedAmount().compareTo(BigDecimal.ZERO) > 0) {
            order.setStatus(OrderStatus.PARTIAL_REFUND);
        }
        orderRepository.save(order);

        auditLogService.log("Order", orderId, "UPDATE",
                "退费 " + request.getRefundAmount() + ", 原因: " + request.getRefundReason());
        return refund;
    }
}
