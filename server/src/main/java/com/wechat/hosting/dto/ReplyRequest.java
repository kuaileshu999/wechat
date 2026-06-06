package com.wechat.hosting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReplyRequest(
        @NotNull Long conversationId,
        @NotNull Long senderUserId,
        @NotBlank String content
) {
}
