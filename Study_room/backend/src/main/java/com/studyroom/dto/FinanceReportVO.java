package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinanceReportVO {
    private LocalDate date;
    private Long campusId;
    private String campusName;
    private String month;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalConsumedAmount;
    private BigDecimal totalPendingAmount;
}
