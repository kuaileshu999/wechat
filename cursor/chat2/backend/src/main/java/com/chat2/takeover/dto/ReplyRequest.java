package com.chat2.takeover.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReplyRequest(
        @NotNull Long conversationId,
        @NotBlank String content,
        String senderName) {

    public String senderName() {
        return senderName != null && !senderName.isBlank() ? senderName : "接管辅导";
    }
}
