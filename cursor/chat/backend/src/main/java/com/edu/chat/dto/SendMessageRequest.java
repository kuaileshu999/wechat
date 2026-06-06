package com.edu.chat.dto;

public record SendMessageRequest(Long conversationId, Long senderId, String content) {
}
