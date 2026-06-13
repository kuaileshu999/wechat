package com.studyroom.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleBatchDeleteRequest {
    @NotEmpty(message = "请选择要删除的排课")
    private List<Long> ids;
}
