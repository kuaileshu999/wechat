package com.wechat.hosting.dto;

import jakarta.validation.constraints.NotNull;

public record TransferConversationRequest(
        @NotNull Long toHandlerUserId,
        @NotNull Long operatorId,
        String remark
) {
}
