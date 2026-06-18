package com.studyroom.dto;

import com.studyroom.entity.Order;
import com.studyroom.entity.OrderRefund;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderDetailVO {
    private Order order;
    private String studentName;
    private String studentPhone;
    private String courseName;
    private String campusName;
    private String salespersonName;
    private List<Long> salespersonIds;
    private String salespersonNames;
    private String teacherName;
    private List<Long> teacherIds;
    private String teacherNames;
    private BigDecimal pendingAmount;
    private BigDecimal pendingHours;
    private List<OrderRefund> refunds;
    private List<com.studyroom.entity.AuditLog> auditLogs;
}
