package com.studyroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleBatchCreateRequest {
    @NotEmpty(message = "请至少添加一条排课")
    @Valid
    private List<ScheduleRequest> items;
}
