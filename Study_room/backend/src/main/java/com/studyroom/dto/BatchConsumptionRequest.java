package com.studyroom.dto;

import com.studyroom.enums.ConsumptionMode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BatchConsumptionRequest {

    @NotEmpty(message = "订单列表不能为空")
    private List<Long> orderIds;

    private ConsumptionMode consumptionMode;

    private BigDecimal consumedAmount;

    private BigDecimal consumedHours;

    private String remark;
}
