package com.edu.chat.dto;

public record AgentProfileDto(
        UserDto user,
        String statusLabel,
        int accountCount,
        int totalUnread,
        int conversionCount,
        int undertakingCount
) {
}
