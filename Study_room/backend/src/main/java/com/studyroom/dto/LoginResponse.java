package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private List<Long> campusIds;
    private List<String> permissions;
    private List<MenuVO> menus;
}
