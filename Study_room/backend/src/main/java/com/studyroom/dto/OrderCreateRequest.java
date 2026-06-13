package com.studyroom.dto;

import com.studyroom.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderCreateRequest {

    @NotNull(message = "校区不能为空")
    private Long campusId;

    @NotNull(message = "学员不能为空")
    private Long studentId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "课时数不能为空")
    @Min(value = 1, message = "课时数必须为正整数")
    private Integer totalHours;

    @NotNull(message = "收款金额不能为空")
    @DecimalMin(value = "1", message = "收款金额必须为正整数")
    private BigDecimal paidAmount;

    @NotNull(message = "收款方式不能为空")
    private PaymentMethod paymentMethod;

    @NotNull(message = "收款日期不能为空")
    private LocalDate paymentDate;

    @NotNull(message = "销售人不能为空")
    private Long salespersonId;

    private String remark;
}
