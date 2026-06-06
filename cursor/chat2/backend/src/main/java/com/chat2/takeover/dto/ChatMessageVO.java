package com.chat2.takeover.dto;

import com.chat2.takeover.enums.MessageDirection;

import java.time.LocalDateTime;

public record ChatMessageVO(
        Long id,
        Long conversationId,
        MessageDirection direction,
        String senderName,
        String content,
        LocalDateTime sentAt) {
}
