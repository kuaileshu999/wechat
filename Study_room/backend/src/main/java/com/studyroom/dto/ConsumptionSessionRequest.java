package com.studyroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsumptionSessionRequest {

    private Long teacherId;

    @NotNull(message = "上课时间不能为空")
    private LocalDateTime classTime;

    @NotNull(message = "结课时间不能为空")
    private LocalDateTime classEndTime;

    private BigDecimal consumedAmount;

    private BigDecimal consumedHours;
}
