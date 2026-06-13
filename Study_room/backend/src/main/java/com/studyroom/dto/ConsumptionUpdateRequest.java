package com.studyroom.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsumptionUpdateRequest {

    private BigDecimal consumedAmount;

    private BigDecimal consumedHours;

    private String remark;
}
