package com.studyroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConsumptionRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private Long subjectId;

    private String remark;

    @NotEmpty(message = "请至少添加一次消课")
    @Valid
    private List<ConsumptionSessionRequest> sessions;
}
