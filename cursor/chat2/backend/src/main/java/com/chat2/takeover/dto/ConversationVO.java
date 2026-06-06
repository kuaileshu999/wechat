package com.chat2.takeover.dto;

import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;

import java.time.LocalDateTime;

public record ConversationVO(
        Long id,
        Long wecomAccountId,
        String wecomAccountName,
        Long tutorId,
        String tutorName,
        Long studentId,
        String studentName,
        ChatType chatType,
        String chatTypeLabel,
        String groupName,
        MessageCategory category,
        String categoryLabel,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        Integer unreadCount,
        Boolean pendingReply,
        Integer pendingReplyCount) {
}
