package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserPermissionVO {
    private Long userId;
    private String username;
    private String realName;
    private Integer enabled;
    private Integer locked;
    private Set<Long> campusIds;
    private Set<Long> roleIds;
}
