package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String code;

    private String description;

    @NotEmpty(message = "至少选择一个权限")
    private Set<Long> permissionIds;
}
