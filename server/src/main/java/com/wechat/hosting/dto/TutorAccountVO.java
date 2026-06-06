package com.wechat.hosting.dto;

public record TutorAccountVO(
        Long id,
        String accountName,
        String subject,
        String grade,
        Integer studentCount,
        Integer status
) {
}
