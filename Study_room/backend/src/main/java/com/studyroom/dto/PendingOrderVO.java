package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PendingOrderVO {
    private Long id;
    private String orderNo;
    private Long campusId;
    private Long studentId;
    private String studentName;
    private String studentPhone;
    private BigDecimal paidAmount;
    private BigDecimal consumedAmount;
    private Integer totalHours;
    private BigDecimal consumedHours;
}
