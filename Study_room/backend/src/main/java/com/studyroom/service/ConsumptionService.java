package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.BatchConsumptionRequest;
import com.studyroom.dto.ConsumptionRequest;
import com.studyroom.dto.ConsumptionUpdateRequest;
import com.studyroom.entity.ConsumptionRecord;
import com.studyroom.entity.Course;
import com.studyroom.entity.Order;
import com.studyroom.enums.ConsumptionMode;
import com.studyroom.repository.ConsumptionRecordRepository;
import com.studyroom.repository.CourseRepository;
import com.studyroom.repository.OrderRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final AuditLogService auditLogService;

    public PageResult<ConsumptionRecord> listCompleted(int page, int size) {
        return listByStatus("COMPLETED", page, size);
    }

    public PageResult<ConsumptionRecord> listPending(int page, int size) {
        return listByStatus("PENDING", page, size);
    }

    public List<Order> listPendingOrders() {
        if (CampusScope.isEmpty()) {
            return List.of();
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        List<Order> pending = new ArrayList<>();
        for (Long campusId : campusIds) {
            orderRepository.findByCampusIdIn(List.of(campusId), PageRequest.of(0, 1000))
                    .forEach(order -> {
                        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
                        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount());
                        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours())
                                .subtract(order.getConsumedHours());
                        if (pendingAmount.compareTo(BigDecimal.ZERO) > 0
                                || pendingHours.compareTo(BigDecimal.ZERO) > 0) {
                            pending.add(order);
                        }
                    });
        }
        return pending;
    }

    @Transactional
    public ConsumptionRecord consume(ConsumptionRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());
        Course course = courseRepository.findById(order.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        ConsumptionMode mode = request.getConsumptionMode() != null
                ? request.getConsumptionMode() : course.getConsumptionMode();
        BigDecimal amount = request.getConsumedAmount() != null ? request.getConsumedAmount() : BigDecimal.ZERO;
        BigDecimal hours = request.getConsumedHours() != null ? request.getConsumedHours() : BigDecimal.ZERO;

        if (mode == ConsumptionMode.AMOUNT) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = course.getUnitAmount();
            }
        } else {
            if (hours.compareTo(BigDecimal.ZERO) <= 0) {
                hours = course.getUnitHours();
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = course.getUnitAmount().multiply(hours);
            }
        }

        validateAndApply(order, amount, hours);

        ConsumptionRecord record = buildRecord(order, mode, amount, hours, null, request.getRemark());
        ConsumptionRecord saved = consumptionRecordRepository.save(record);
        auditLogService.log("ConsumptionRecord", saved.getId(), "CREATE",
                "消课 金额" + amount + " 课时" + hours);
        return saved;
    }

    @Transactional
    public List<ConsumptionRecord> batchConsume(BatchConsumptionRequest request) {
        if (request.getOrderIds().size() < 2) {
            throw new BusinessException("批量消课至少需要2个订单");
        }
        String batchNo = "BATCH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        List<ConsumptionRecord> records = new ArrayList<>();
        Long firstCourseId = null;
        for (Long orderId : request.getOrderIds()) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException("订单不存在: " + orderId));
            if (firstCourseId == null) {
                firstCourseId = order.getCourseId();
            } else if (!firstCourseId.equals(order.getCourseId())) {
                throw new BusinessException("批量消课要求课程类型一致");
            }
            SecurityUtils.checkCampusAccess(order.getCampusId());
            Course course = courseRepository.findById(order.getCourseId())
                    .orElseThrow(() -> new BusinessException("课程不存在"));

            ConsumptionMode mode = request.getConsumptionMode() != null
                    ? request.getConsumptionMode() : course.getConsumptionMode();
            BigDecimal amount = request.getConsumedAmount() != null ? request.getConsumedAmount() : course.getUnitAmount();
            BigDecimal hours = request.getConsumedHours() != null ? request.getConsumedHours() : course.getUnitHours();

            validateAndApply(order, amount, hours);
            ConsumptionRecord record = buildRecord(order, mode, amount, hours, batchNo, request.getRemark());
            records.add(consumptionRecordRepository.save(record));
        }
        auditLogService.log("ConsumptionRecord", 0L, "CREATE", "批量消课 " + records.size() + " 条");
        return records;
    }

    @Transactional
    public ConsumptionRecord update(Long id, ConsumptionUpdateRequest request) {
        ConsumptionRecord record = consumptionRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("消课记录不存在"));
        SecurityUtils.checkCampusAccess(record.getCampusId());
        Order order = orderRepository.findById(record.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));

        BigDecimal oldAmount = record.getConsumedAmount();
        BigDecimal oldHours = record.getConsumedHours();
        BigDecimal newAmount = request.getConsumedAmount() != null ? request.getConsumedAmount() : oldAmount;
        BigDecimal newHours = request.getConsumedHours() != null ? request.getConsumedHours() : oldHours;

        order.setConsumedAmount(order.getConsumedAmount().subtract(oldAmount).add(newAmount));
        order.setConsumedHours(order.getConsumedHours().subtract(oldHours).add(newHours));
        orderRepository.save(order);

        record.setConsumedAmount(newAmount);
        record.setConsumedHours(newHours);
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        ConsumptionRecord saved = consumptionRecordRepository.save(record);
        auditLogService.log("ConsumptionRecord", saved.getId(), "UPDATE",
                "修改消课: 金额 " + oldAmount + "->" + newAmount + ", 课时 " + oldHours + "->" + newHours);
        return saved;
    }

    private PageResult<ConsumptionRecord> listByStatus(String status, int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        Page<ConsumptionRecord> result = consumptionRecordRepository
                .findByCampusIdInAndStatus(campusIds, status, PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    private void validateAndApply(Order order, BigDecimal amount, BigDecimal hours) {
        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount());
        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours()).subtract(order.getConsumedHours());

        if (amount.compareTo(pendingAmount) > 0) {
            throw new BusinessException("消课金额超过待消课金额");
        }
        if (hours.compareTo(pendingHours) > 0) {
            throw new BusinessException("消课课时超过待消课时");
        }

        order.setConsumedAmount(order.getConsumedAmount().add(amount));
        order.setConsumedHours(order.getConsumedHours().add(hours));
        orderRepository.save(order);
    }

    private ConsumptionRecord buildRecord(Order order, ConsumptionMode mode, BigDecimal amount,
                                          BigDecimal hours, String batchNo, String remark) {
        ConsumptionRecord record = new ConsumptionRecord();
        record.setOrderId(order.getId());
        record.setCampusId(order.getCampusId());
        record.setStudentId(order.getStudentId());
        record.setCourseId(order.getCourseId());
        record.setConsumptionMode(mode);
        record.setConsumedAmount(amount);
        record.setConsumedHours(hours);
        record.setStatus("COMPLETED");
        record.setBatchNo(batchNo);
        record.setRemark(remark);
        record.setCreatedBy(SecurityUtils.getCurrentUserId());
        return record;
    }
}
