package com.studyroom.dto;

import com.studyroom.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    @NotNull(message = "退费金额不能为空")
    @DecimalMin(value = "0.01", message = "退费金额必须大于0")
    private BigDecimal refundAmount;

    @NotBlank(message = "退费原因不能为空")
    private String refundReason;

    @NotNull(message = "退款方式不能为空")
    private PaymentMethod refundMethod;

    @Size(max = 200, message = "备注最多200字")
    private String remark;
}
