package com.studyroom.dto;

import com.studyroom.enums.ConsumptionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseUpdateRequest {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @NotEmpty(message = "请选择学科")
    private List<Long> subjectIds;

    @NotNull(message = "请选择年级")
    private Long gradeId;

    @NotNull(message = "请选择消课方式")
    private ConsumptionMode consumptionMode;

    private BigDecimal unitAmount;

    private BigDecimal unitHours;

    @NotNull(message = "请填写每次消课时长")
    @Min(value = 1, message = "每次消课时长必须为正整数")
    private Integer sessionMinutes;
}
