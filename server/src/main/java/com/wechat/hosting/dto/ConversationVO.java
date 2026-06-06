package com.wechat.hosting.dto;

import java.time.LocalDateTime;

public record ConversationVO(
        Long id,
        Integer convType,
        String convTypeLabel,
        Long tutorId,
        String tutorName,
        Long tutorAccountId,
        String accountName,
        String subject,
        String grade,
        Long studentId,
        String studentName,
        String studentAvatar,
        Long groupId,
        String groupName,
        Integer stage,
        String stageLabel,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        Integer unreadCount,
        Long handlerUserId,
        String handlerName
) {
}
