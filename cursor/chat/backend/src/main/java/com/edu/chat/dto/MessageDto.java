package com.edu.chat.dto;

import com.edu.chat.model.Message;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long conversationId,
        UserDto sender,
        String content,
        Instant sentAt,
        String timeLabel,
        boolean containsMath,
        boolean outgoing
) {

    public static MessageDto from(Message m, Long currentUserId, String timeLabel) {
        boolean outgoing = !m.getSender().getRole().name().equals("STUDENT");
        return new MessageDto(
                m.getId(),
                m.getConversation().getId(),
                UserDto.from(m.getSender()),
                m.getContent(),
                m.getSentAt(),
                timeLabel,
                m.isContainsMath(),
                outgoing
        );
    }
}
