package com.studyroom.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserPermissionRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private Integer enabled = 1;
    @NotEmpty(message = "至少选择一个校区")
    private Set<Long> campusIds;
    @NotEmpty(message = "至少选择一个角色")
    private Set<Long> roleIds;
}
