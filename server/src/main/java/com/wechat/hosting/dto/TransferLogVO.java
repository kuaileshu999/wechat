package com.wechat.hosting.dto;

import java.time.LocalDateTime;

public record TransferLogVO(
        Long id,
        Long tutorId,
        String tutorName,
        Long conversationId,
        String conversationTitle,
        Long fromHandlerUserId,
        String fromHandlerName,
        Long toHandlerUserId,
        String toHandlerName,
        Integer actionType,
        String actionTypeLabel,
        Long operatorId,
        String operatorName,
        String remark,
        LocalDateTime createdAt
) {
}
