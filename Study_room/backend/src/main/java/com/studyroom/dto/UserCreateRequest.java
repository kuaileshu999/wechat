package com.studyroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UserCreateRequest {
    @NotNull(message = "请选择员工")
    private Long employeeId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "至少选择一个校区")
    private Set<Long> campusIds;

    @NotEmpty(message = "至少选择一个角色")
    private Set<Long> roleIds;
}
