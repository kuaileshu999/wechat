package com.chat2.takeover.dto;

public record TakeoverAssignmentVO(
        Long id,
        Long sourceTutorId,
        String sourceTutorName,
        Long wecomAccountId,
        String wecomAccountName,
        Boolean allAccounts,
        Long takeoverTutorId,
        String takeoverTutorName,
        String gradeLevel,
        String subject,
        String orgPath,
        Boolean enabled) {
}
