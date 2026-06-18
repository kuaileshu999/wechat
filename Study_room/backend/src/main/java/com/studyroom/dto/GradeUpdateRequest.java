package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GradeUpdateRequest {

    @NotBlank(message = "年级名称不能为空")
    private String name;
}
