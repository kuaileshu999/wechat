package com.studyroom.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String username;
    private List<Long> campusIds;
}
