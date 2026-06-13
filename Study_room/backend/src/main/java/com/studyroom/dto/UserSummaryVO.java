package com.studyroom.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryVO {
    private Long id;
    private String username;
    private String realName;
}
