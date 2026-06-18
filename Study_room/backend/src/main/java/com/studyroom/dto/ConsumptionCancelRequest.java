package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsumptionCancelRequest {

    @NotBlank(message = "请填写取消原因")
    private String cancelReason;
}
