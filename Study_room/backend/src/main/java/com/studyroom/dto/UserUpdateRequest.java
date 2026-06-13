package com.studyroom.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateRequest {
    @NotNull(message = "账号状态不能为空")
    private Integer enabled = 1;

    @NotEmpty(message = "至少选择一个校区")
    private Set<Long> campusIds;

    @NotEmpty(message = "至少选择一个角色")
    private Set<Long> roleIds;
}
