package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampusRequest {

    @NotBlank(message = "校区名称不能为空")
    private String name;
}
