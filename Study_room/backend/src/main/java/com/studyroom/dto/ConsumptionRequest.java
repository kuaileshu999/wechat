package com.studyroom.dto;

import com.studyroom.enums.ConsumptionMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsumptionRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private ConsumptionMode consumptionMode;

    private BigDecimal consumedAmount;

    private BigDecimal consumedHours;

    private String remark;
}
