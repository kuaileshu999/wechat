package com.wechat.hosting.dto;

import java.time.LocalDateTime;

public record MessageVO(
        Long id,
        Long conversationId,
        Integer senderType,
        Long senderId,
        String senderName,
        Integer contentType,
        String content,
        String mediaUrl,
        LocalDateTime sentAt
) {
}
