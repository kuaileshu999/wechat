package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private Long employeeId;
    private String employeeName;
    private Integer enabled;
    private Integer locked;
    private Set<Long> campusIds;
    private List<String> campusNames;
    private Set<Long> roleIds;
    private List<String> roleNames;
}
