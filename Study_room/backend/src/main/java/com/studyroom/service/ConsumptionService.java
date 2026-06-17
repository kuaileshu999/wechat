package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.*;
import com.studyroom.entity.ConsumptionRecord;
import com.studyroom.entity.Course;
import com.studyroom.entity.Employee;
import com.studyroom.entity.Order;
import com.studyroom.entity.Student;
import com.studyroom.enums.ConsumptionMode;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.repository.ConsumptionRecordRepository;
import com.studyroom.repository.CourseRepository;
import com.studyroom.repository.EmployeeRepository;
import com.studyroom.repository.OrderRepository;
import com.studyroom.repository.StudentRepository;
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
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final AuditLogService auditLogService;

    public PageResult<ConsumptionRecordVO> listCompleted(int page, int size, String keyword) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        String kw = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        Page<ConsumptionRecord> result = consumptionRecordRepository
                .searchCompleted(campusIds, "COMPLETED", kw, PageRequest.of(page - 1, size));
        List<ConsumptionRecordVO> list = result.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotalElements(), page, size);
    }

    public List<PendingOrderVO> listPendingOrders(String keyword) {
        if (CampusScope.isEmpty()) {
            return List.of();
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        String kw = keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase() : null;
        List<PendingOrderVO> pending = new ArrayList<>();
        for (Long campusId : campusIds) {
            orderRepository.findByCampusIdIn(List.of(campusId), PageRequest.of(0, 1000))
                    .forEach(order -> {
                        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
                        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount());
                        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours())
                                .subtract(order.getConsumedHours());
                        if (pendingAmount.compareTo(BigDecimal.ZERO) > 0
                                || pendingHours.compareTo(BigDecimal.ZERO) > 0) {
                            Student student = studentRepository.findById(order.getStudentId()).orElse(null);
                            String studentName = student != null ? student.getName() : "";
                            String studentPhone = student != null ? student.getPhone() : "";
                            if (kw != null) {
                                String name = studentName.toLowerCase();
                                String phone = studentPhone;
                                if (!name.contains(kw) && !phone.contains(kw)) {
                                    return;
                                }
                            }
                            pending.add(PendingOrderVO.builder()
                                    .id(order.getId())
                                    .orderNo(order.getOrderNo())
                                    .campusId(order.getCampusId())
                                    .studentId(order.getStudentId())
                                    .studentName(studentName)
                                    .studentPhone(studentPhone)
                                    .paidAmount(order.getPaidAmount())
                                    .consumedAmount(order.getConsumedAmount())
                                    .totalHours(order.getTotalHours())
                                    .consumedHours(order.getConsumedHours())
                                    .build());
                        }
                    });
        }
        return pending;
    }

    public ConsumptionOrderContextVO getOrderContext(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());
        Course course = courseRepository.findById(order.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount()).max(BigDecimal.ZERO);
        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours())
                .subtract(order.getConsumedHours()).max(BigDecimal.ZERO);

        String teacherName = null;
        if (order.getTeacherId() != null) {
            teacherName = employeeRepository.findById(order.getTeacherId())
                    .map(Employee::getName).orElse(null);
        }

        return ConsumptionOrderContextVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .campusId(order.getCampusId())
                .teacherId(order.getTeacherId())
                .teacherName(teacherName)
                .courseName(course.getName())
                .consumptionMode(course.getConsumptionMode())
                .unitAmount(course.getUnitAmount())
                .unitHours(course.getUnitHours())
                .sessionMinutes(course.getSessionMinutes())
                .pendingAmount(pendingAmount)
                .pendingHours(pendingHours)
                .completedRecords(listOrderRecords(order.getId()))
                .build();
    }

    public List<ConsumptionRecordVO> listOrderRecords(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());
        return consumptionRecordRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public List<ConsumptionRecord> consume(ConsumptionRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());
        Course course = courseRepository.findById(order.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        ConsumptionMode mode = course.getConsumptionMode();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalHours = BigDecimal.ZERO;
        List<ResolvedSession> resolvedSessions = new ArrayList<>();

        for (int i = 0; i < request.getSessions().size(); i++) {
            ConsumptionSessionRequest session = request.getSessions().get(i);
            try {
                ResolvedSession resolved = resolveSession(session, order, course, mode, i + 1);
                totalAmount = totalAmount.add(resolved.amount());
                totalHours = totalHours.add(resolved.hours());
                resolvedSessions.add(resolved);
            } catch (BusinessException e) {
                throw new BusinessException("第 " + (i + 1) + " 次消课: " + e.getMessage());
            }
        }

        validateAndApply(order, totalAmount, totalHours);

        String batchNo = resolvedSessions.size() > 1 ? generateBatchNo() : null;
        List<ConsumptionRecord> saved = new ArrayList<>();
        for (ResolvedSession resolved : resolvedSessions) {
            ConsumptionRecord record = buildRecord(order, mode, resolved.amount(), resolved.hours(),
                    batchNo, request.getRemark(), resolved.teacherId(),
                    resolved.classTime(), resolved.classEndTime());
            saved.add(consumptionRecordRepository.save(record));
        }

        auditLogService.log("ConsumptionRecord", saved.get(0).getId(), "CREATE",
                "消课 " + saved.size() + " 次, 金额" + totalAmount + " 课时" + totalHours);
        return saved;
    }

    @Transactional
    public List<ConsumptionRecord> batchConsume(BatchConsumptionRequest request) {
        if (request.getOrderIds().size() < 2) {
            throw new BusinessException("批量消课至少需要2个订单");
        }
        String batchNo = generateBatchNo();

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

            ConsumptionMode mode = course.getConsumptionMode();
            BigDecimal amount;
            BigDecimal hours;
            if (mode == ConsumptionMode.AMOUNT) {
                amount = request.getConsumedAmount() != null ? request.getConsumedAmount() : course.getUnitAmount();
                hours = BigDecimal.ZERO;
            } else {
                hours = request.getConsumedHours() != null ? request.getConsumedHours() : course.getUnitHours();
                amount = course.getUnitAmount().multiply(hours);
            }

            validateAndApply(order, amount, hours);
            LocalDateTime classTime = normalizeClassTime(LocalDateTime.now());
            LocalDateTime classEndTime = classTime.plusMinutes(
                    course.getSessionMinutes() != null ? course.getSessionMinutes() : 60);
            Long teacherId = resolveTeacherId(order.getTeacherId(), order.getCampusId());
            ConsumptionRecord record = buildRecord(order, mode, amount, hours, batchNo,
                    request.getRemark(), teacherId, classTime, classEndTime);
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
        Course course = courseRepository.findById(record.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        BigDecimal oldAmount = record.getConsumedAmount();
        BigDecimal oldHours = record.getConsumedHours();
        BigDecimal newAmount = request.getConsumedAmount() != null ? request.getConsumedAmount() : oldAmount;
        BigDecimal newHours = request.getConsumedHours() != null ? request.getConsumedHours() : oldHours;

        if (course.getConsumptionMode() == ConsumptionMode.AMOUNT) {
            if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消课金额必须大于0");
            }
            newHours = BigDecimal.ZERO;
        } else {
            if (newHours.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消课课时必须大于0");
            }
        }

        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
        BigDecimal pendingAmount = effectivePaid.subtract(order.getConsumedAmount()).add(oldAmount);
        BigDecimal pendingHours = BigDecimal.valueOf(order.getTotalHours())
                .subtract(order.getConsumedHours()).add(oldHours);
        if (newAmount.compareTo(pendingAmount) > 0) {
            throw new BusinessException("消课金额超过待消课金额");
        }
        if (newHours.compareTo(pendingHours) > 0) {
            throw new BusinessException("消课课时超过待消课时");
        }

        order.setConsumedAmount(order.getConsumedAmount().subtract(oldAmount).add(newAmount));
        order.setConsumedHours(order.getConsumedHours().subtract(oldHours).add(newHours));
        orderRepository.save(order);

        record.setConsumedAmount(newAmount);
        record.setConsumedHours(newHours);
        if (request.getTeacherId() != null) {
            validateTeacher(request.getTeacherId(), order.getCampusId());
            record.setTeacherId(request.getTeacherId());
        }
        if (request.getClassTime() != null) {
            LocalDateTime classTime = normalizeClassTime(request.getClassTime());
            record.setClassTime(classTime);
            if (request.getClassEndTime() != null) {
                LocalDateTime classEndTime = normalizeClassTime(request.getClassEndTime());
                validateClassTimeRange(classTime, classEndTime);
                record.setClassEndTime(classEndTime);
            } else if (record.getClassEndTime() != null && !record.getClassEndTime().isAfter(classTime)) {
                record.setClassEndTime(classTime.plusMinutes(
                        course.getSessionMinutes() != null ? course.getSessionMinutes() : 60));
            }
        } else if (request.getClassEndTime() != null) {
            LocalDateTime classEndTime = normalizeClassTime(request.getClassEndTime());
            validateClassTimeRange(record.getClassTime(), classEndTime);
            record.setClassEndTime(classEndTime);
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        ConsumptionRecord saved = consumptionRecordRepository.save(record);
        auditLogService.log("ConsumptionRecord", saved.getId(), "UPDATE",
                "修改消课: 金额 " + oldAmount + "->" + newAmount + ", 课时 " + oldHours + "->" + newHours);
        return saved;
    }

    private record ResolvedSession(BigDecimal amount, BigDecimal hours, Long teacherId,
                                   LocalDateTime classTime, LocalDateTime classEndTime) {
    }

    private ResolvedSession resolveSession(ConsumptionSessionRequest session, Order order,
                                           Course course, ConsumptionMode mode, int rowNum) {
        LocalDateTime classTime = normalizeClassTime(session.getClassTime());
        LocalDateTime classEndTime = session.getClassEndTime() != null
                ? normalizeClassTime(session.getClassEndTime())
                : classTime.plusMinutes(course.getSessionMinutes() != null ? course.getSessionMinutes() : 60);
        validateClassTimeRange(classTime, classEndTime);
        Long teacherId = session.getTeacherId() != null
                ? session.getTeacherId() : order.getTeacherId();
        teacherId = resolveTeacherId(teacherId, order.getCampusId());

        BigDecimal amount;
        BigDecimal hours;
        if (mode == ConsumptionMode.AMOUNT) {
            amount = session.getConsumedAmount() != null && session.getConsumedAmount().compareTo(BigDecimal.ZERO) > 0
                    ? session.getConsumedAmount() : course.getUnitAmount();
            hours = BigDecimal.ZERO;
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消课金额必须大于0");
            }
        } else {
            hours = session.getConsumedHours() != null && session.getConsumedHours().compareTo(BigDecimal.ZERO) > 0
                    ? session.getConsumedHours() : course.getUnitHours();
            if (hours.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消课课时必须大于0");
            }
            amount = course.getUnitAmount().multiply(hours);
        }
        return new ResolvedSession(amount, hours, teacherId, classTime, classEndTime);
    }

    private Long resolveTeacherId(Long teacherId, Long campusId) {
        if (teacherId == null) {
            return null;
        }
        validateTeacher(teacherId, campusId);
        return teacherId;
    }

    private void validateTeacher(Long teacherId, Long campusId) {
        Employee teacher = employeeRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("上课老师不存在"));
        if (teacher.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessException("上课老师已离职");
        }
        if (!teacher.getCampusId().equals(campusId)) {
            throw new BusinessException("上课老师不属于该校区");
        }
    }

    private void validateClassTimeRange(LocalDateTime classTime, LocalDateTime classEndTime) {
        if (classTime == null || classEndTime == null) {
            throw new BusinessException("上课时间与结课时间不能为空");
        }
        if (!classEndTime.isAfter(classTime)) {
            throw new BusinessException("结课时间必须在开始时间之后");
        }
    }

    private LocalDateTime normalizeClassTime(LocalDateTime time) {
        if (time == null) {
            throw new BusinessException("上课时间不能为空");
        }
        LocalDateTime normalized = time.withSecond(0).withNano(0);
        if (normalized.getMinute() % 15 != 0) {
            throw new BusinessException("上课时间必须为15分钟的整数倍");
        }
        return normalized;
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
                                          BigDecimal hours, String batchNo, String remark,
                                          Long teacherId, LocalDateTime classTime, LocalDateTime classEndTime) {
        ConsumptionRecord record = new ConsumptionRecord();
        record.setOrderId(order.getId());
        record.setCampusId(order.getCampusId());
        record.setStudentId(order.getStudentId());
        record.setCourseId(order.getCourseId());
        record.setTeacherId(teacherId);
        record.setClassTime(classTime);
        record.setClassEndTime(classEndTime);
        record.setConsumptionMode(mode);
        record.setConsumedAmount(amount);
        record.setConsumedHours(hours);
        record.setStatus("COMPLETED");
        record.setBatchNo(batchNo);
        record.setRemark(remark);
        record.setCreatedBy(SecurityUtils.getCurrentUserId());
        return record;
    }

    private ConsumptionRecordVO toVO(ConsumptionRecord record) {
        String teacherName = record.getTeacherId() == null ? null
                : employeeRepository.findById(record.getTeacherId()).map(Employee::getName).orElse(null);
        Student student = studentRepository.findById(record.getStudentId()).orElse(null);
        Course course = courseRepository.findById(record.getCourseId()).orElse(null);
        Integer sessionMinutes = course != null ? course.getSessionMinutes() : null;
        LocalDateTime classEndTime = record.getClassEndTime();
        if (classEndTime == null && record.getClassTime() != null && sessionMinutes != null) {
            classEndTime = record.getClassTime().plusMinutes(sessionMinutes);
        }
        return ConsumptionRecordVO.builder()
                .id(record.getId())
                .orderId(record.getOrderId())
                .campusId(record.getCampusId())
                .studentId(record.getStudentId())
                .studentName(student != null ? student.getName() : "")
                .studentPhone(student != null ? student.getPhone() : "")
                .courseId(record.getCourseId())
                .teacherId(record.getTeacherId())
                .teacherName(teacherName)
                .consumptionMode(record.getConsumptionMode())
                .consumedAmount(record.getConsumedAmount())
                .consumedHours(record.getConsumedHours())
                .status(record.getStatus())
                .batchNo(record.getBatchNo())
                .remark(record.getRemark())
                .classTime(record.getClassTime())
                .classEndTime(classEndTime)
                .sessionMinutes(sessionMinutes)
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String generateBatchNo() {
        return "BATCH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
