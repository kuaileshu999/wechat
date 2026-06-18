package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectUpdateRequest {

    @NotBlank(message = "学科名称不能为空")
    private String name;
}
