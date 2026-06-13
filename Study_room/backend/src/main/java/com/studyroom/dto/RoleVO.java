package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Set<Long> permissionIds;
    private List<String> permissionNames;
}
