package com.chat2.takeover.dto;

public record WecomAccountVO(
        Long id,
        Long tutorId,
        String tutorName,
        String accountName,
        String wecomUserId,
        String gradeLevel,
        String subject,
        String displayLabel) {
}
