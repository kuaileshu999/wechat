package com.studyroom.dto;

import com.studyroom.enums.OrderStatus;
import com.studyroom.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OrderListVO {
    private Long id;
    private String orderNo;
    private Long campusId;
    private String campusName;
    private String studentName;
    private String studentPhone;
    private String courseName;
    private String unionPayOrderNo;
    private String teacherNames;
    private String salespersonNames;
    private Integer totalHours;
    private BigDecimal paidAmount;
    private BigDecimal consumedAmount;
    private BigDecimal consumedHours;
    private BigDecimal refundedAmount;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private OrderStatus status;
    private String remark;
}
