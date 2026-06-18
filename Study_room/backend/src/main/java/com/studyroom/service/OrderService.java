package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.OrderCreateRequest;
import com.studyroom.dto.OrderDetailVO;
import com.studyroom.dto.OrderListVO;
import com.studyroom.dto.OrderUpdateRequest;
import com.studyroom.dto.RefundRequest;
import com.studyroom.entity.*;
import com.studyroom.enums.OrderStatus;
import com.studyroom.enums.PaymentMethod;
import com.studyroom.repository.*;
import com.studyroom.security.SecurityUtils;
import com.studyroom.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTeacherRepository orderTeacherRepository;
    private final OrderSalespersonRepository orderSalespersonRepository;
    private final OrderRefundRepository refundRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CampusRepository campusRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final CampusService campusService;

    public PageResult<OrderListVO> list(Long campusId, LocalDate startDate, LocalDate endDate,
                                        String keyword, String unionPayOrderNo, String orderNo, int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
        }
        String kw = normalizeKeyword(keyword);
        String unionNo = normalizeKeyword(unionPayOrderNo);
        String orderNoKw = normalizeKeyword(orderNo);
        Page<Order> result = orderRepository.search(campusIds, campusId, startDate, endDate, kw, unionNo, orderNoKw,
                PageRequest.of(page - 1, size));
        return new PageResult<>(toListVO(result.getContent()), result.getTotalElements(), page, size);
    }

    public List<OrderListVO> listAllForExport(Long campusId, LocalDate startDate, LocalDate endDate,
                                              String keyword, String unionPayOrderNo, String orderNo) {
        if (CampusScope.isEmpty()) {
            return List.of();
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
        }
        String kw = normalizeKeyword(keyword);
        String unionNo = normalizeKeyword(unionPayOrderNo);
        String orderNoKw = normalizeKeyword(orderNo);
        Page<Order> result = orderRepository.search(campusIds, campusId, startDate, endDate, kw, unionNo, orderNoKw,
                PageRequest.of(0, 10000));
        return toListVO(result.getContent());
    }

    public OrderDetailVO getDetail(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        SecurityUtils.checkCampusAccess(order.getCampusId());

        Student student = studentRepository.findById(order.getStudentId()).orElse(null);
        Course course = courseRepository.findById(order.getCourseId()).orElse(null);
        Campus campus = campusRepository.findById(order.getCampusId()).orElse(null);
        List<Long> salespersonIds = getSalespersonIds(order);
        String salespersonNames = resolveEmployeeNames(salespersonIds);
        List<Long> teacherIds = getTeacherIds(order);
        String teacherNames = resolveEmployeeNames(teacherIds);

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
                .salespersonName(salespersonNames)
                .salespersonNames(salespersonNames)
                .salespersonIds(salespersonIds)
                .teacherName(teacherNames)
                .teacherNames(teacherNames)
                .teacherIds(teacherIds)
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

        List<Long> teacherIds = validateTeacherIds(request.getTeacherIds(), request.getCampusId());
        List<Long> salespersonIds = validateSalespersonIds(request.getSalespersonIds(), request.getCampusId());

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setCampusId(request.getCampusId());
        order.setStudentId(request.getStudentId());
        order.setCourseId(request.getCourseId());
        order.setTotalHours(request.getTotalHours());
        order.setPaidAmount(request.getPaidAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setUnionPayOrderNo(normalizeUnionPayOrderNo(request.getPaymentMethod(), request.getUnionPayOrderNo()));
        order.setPaymentDate(request.getPaymentDate());
        order.setSalespersonId(salespersonIds.get(0));
        order.setTeacherId(teacherIds.isEmpty() ? null : teacherIds.get(0));
        order.setRemark(request.getRemark());
        order.setCreatedBy(SecurityUtils.getCurrentUserId());

        Order saved = orderRepository.save(order);
        saveOrderSalespersons(saved.getId(), salespersonIds);
        saveOrderTeachers(saved.getId(), teacherIds);
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

        List<Long> teacherIds = validateTeacherIds(request.getTeacherIds(), order.getCampusId());
        List<Long> salespersonIds = validateSalespersonIds(request.getSalespersonIds(), order.getCampusId());

        order.setStudentId(request.getStudentId());
        order.setCourseId(request.getCourseId());
        order.setTotalHours(request.getTotalHours());
        order.setPaidAmount(request.getPaidAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setUnionPayOrderNo(normalizeUnionPayOrderNo(request.getPaymentMethod(), request.getUnionPayOrderNo()));
        order.setPaymentDate(request.getPaymentDate());
        order.setSalespersonId(salespersonIds.get(0));
        order.setTeacherId(teacherIds.isEmpty() ? null : teacherIds.get(0));
        order.setRemark(request.getRemark());

        Order saved = orderRepository.save(order);
        saveOrderSalespersons(saved.getId(), salespersonIds);
        saveOrderTeachers(saved.getId(), teacherIds);
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

    private List<OrderListVO> toListVO(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Set<Long> studentIds = orders.stream().map(Order::getStudentId).collect(Collectors.toSet());
        Set<Long> courseIds = orders.stream().map(Order::getCourseId).collect(Collectors.toSet());
        Set<Long> campusIds = orders.stream().map(Order::getCampusId).collect(Collectors.toSet());
        Set<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());

        Map<Long, Student> students = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, String> courseNames = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));
        Map<Long, String> campusNames = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(Campus::getId, Campus::getName));

        Map<Long, List<Long>> salespeopleByOrder = orderSalespersonRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(OrderSalesperson::getOrderId,
                        Collectors.mapping(OrderSalesperson::getSalespersonId, Collectors.toList())));

        Map<Long, List<Long>> teachersByOrder = orderTeacherRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(OrderTeacher::getOrderId,
                        Collectors.mapping(OrderTeacher::getTeacherId, Collectors.toList())));

        Set<Long> allEmployeeIds = new HashSet<>();
        for (Order order : orders) {
            List<Long> salespersonIds = salespeopleByOrder.getOrDefault(order.getId(), List.of());
            if (salespersonIds.isEmpty() && order.getSalespersonId() != null) {
                salespersonIds = List.of(order.getSalespersonId());
            }
            salespeopleByOrder.put(order.getId(), salespersonIds);
            allEmployeeIds.addAll(salespersonIds);

            List<Long> teacherIds = teachersByOrder.getOrDefault(order.getId(), List.of());
            if (teacherIds.isEmpty() && order.getTeacherId() != null) {
                teacherIds = List.of(order.getTeacherId());
            }
            teachersByOrder.put(order.getId(), teacherIds);
            allEmployeeIds.addAll(teacherIds);
        }
        Map<Long, String> employeeNames = employeeRepository.findAllById(allEmployeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<OrderListVO> list = new ArrayList<>();
        for (Order order : orders) {
            List<Long> salespersonIds = salespeopleByOrder.getOrDefault(order.getId(), List.of());
            String salespersonNames = salespersonIds.stream()
                    .map(employeeNames::get)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining("、"));
            List<Long> teacherIds = teachersByOrder.getOrDefault(order.getId(), List.of());
            String teacherNames = teacherIds.stream()
                    .map(employeeNames::get)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining("、"));
            list.add(OrderListVO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .campusId(order.getCampusId())
                    .campusName(campusNames.getOrDefault(order.getCampusId(), ""))
                    .studentName(students.containsKey(order.getStudentId())
                            ? students.get(order.getStudentId()).getName() : "")
                    .studentPhone(students.containsKey(order.getStudentId())
                            ? students.get(order.getStudentId()).getPhone() : "")
                    .courseName(courseNames.getOrDefault(order.getCourseId(), ""))
                    .unionPayOrderNo(order.getUnionPayOrderNo())
                    .teacherNames(teacherNames)
                    .salespersonNames(salespersonNames)
                    .paidAmount(order.getPaidAmount())
                    .consumedAmount(order.getConsumedAmount())
                    .consumedHours(order.getConsumedHours())
                    .refundedAmount(order.getRefundedAmount())
                    .totalHours(order.getTotalHours())
                    .paymentMethod(order.getPaymentMethod())
                    .paymentDate(order.getPaymentDate())
                    .status(order.getStatus())
                    .remark(order.getRemark())
                    .build());
        }
        return list;
    }

    private List<Long> getSalespersonIds(Order order) {
        List<Long> salespersonIds = orderSalespersonRepository.findByOrderId(order.getId()).stream()
                .map(OrderSalesperson::getSalespersonId)
                .toList();
        if (salespersonIds.isEmpty() && order.getSalespersonId() != null) {
            return List.of(order.getSalespersonId());
        }
        return salespersonIds;
    }

    private List<Long> getTeacherIds(Order order) {
        List<Long> teacherIds = orderTeacherRepository.findByOrderId(order.getId()).stream()
                .map(OrderTeacher::getTeacherId)
                .toList();
        if (teacherIds.isEmpty() && order.getTeacherId() != null) {
            return List.of(order.getTeacherId());
        }
        return teacherIds;
    }

    private String resolveEmployeeNames(List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return "";
        }
        return employeeRepository.findAllById(employeeIds).stream()
                .map(Employee::getName)
                .collect(Collectors.joining("、"));
    }

    private void saveOrderSalespersons(Long orderId, List<Long> salespersonIds) {
        orderSalespersonRepository.deleteByOrderId(orderId);
        for (Long salespersonId : salespersonIds) {
            OrderSalesperson os = new OrderSalesperson();
            os.setOrderId(orderId);
            os.setSalespersonId(salespersonId);
            orderSalespersonRepository.save(os);
        }
    }

    private void saveOrderTeachers(Long orderId, List<Long> teacherIds) {
        orderTeacherRepository.deleteByOrderId(orderId);
        for (Long teacherId : teacherIds) {
            OrderTeacher ot = new OrderTeacher();
            ot.setOrderId(orderId);
            ot.setTeacherId(teacherId);
            orderTeacherRepository.save(ot);
        }
    }

    private List<Long> validateSalespersonIds(List<Long> salespersonIds, Long campusId) {
        if (salespersonIds == null || salespersonIds.isEmpty()) {
            throw new BusinessException("请至少选择一位销售人");
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (Long salespersonId : salespersonIds) {
            if (salespersonId != null) {
                validateSalesperson(salespersonId, campusId);
                ids.add(salespersonId);
            }
        }
        if (ids.isEmpty()) {
            throw new BusinessException("请至少选择一位销售人");
        }
        return new ArrayList<>(ids);
    }

    private void validateSalesperson(Long salespersonId, Long campusId) {
        Employee salesperson = employeeRepository.findById(salespersonId)
                .orElseThrow(() -> new BusinessException("销售人不存在"));
        if (!salesperson.getCampusId().equals(campusId)) {
            throw new BusinessException("销售人不属于该校区");
        }
        if (salesperson.getEmploymentStatus() != com.studyroom.enums.EmploymentStatus.ACTIVE) {
            throw new BusinessException("销售人已离职");
        }
    }

    private List<Long> validateTeacherIds(List<Long> teacherIds, Long campusId) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (Long teacherId : teacherIds) {
            if (teacherId != null) {
                validateTeacher(teacherId, campusId);
                ids.add(teacherId);
            }
        }
        return new ArrayList<>(ids);
    }

    private void validateTeacher(Long teacherId, Long campusId) {
        Employee teacher = employeeRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("上课老师不存在"));
        if (!teacher.getCampusId().equals(campusId)) {
            throw new BusinessException("上课老师不属于该校区");
        }
        if (teacher.getEmploymentStatus() != com.studyroom.enums.EmploymentStatus.ACTIVE) {
            throw new BusinessException("上课老师已离职");
        }
    }

    private String normalizeUnionPayOrderNo(PaymentMethod paymentMethod, String unionPayOrderNo) {
        if (paymentMethod != PaymentMethod.UNION_PAY) {
            return null;
        }
        if (unionPayOrderNo == null || unionPayOrderNo.isBlank()) {
            throw new BusinessException("请填写银联订单号");
        }
        return unionPayOrderNo.trim();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
