package com.edu.chat.dto;

import com.edu.chat.model.Conversation;
import com.edu.chat.model.ConversationType;
import com.edu.chat.model.PeriodPhase;

import java.time.Instant;
import java.util.List;

public record ConversationDto(
        Long id,
        ConversationType type,
        UserDto student,
        String lastMessagePreview,
        Instant lastMessageAt,
        String lastMessageTimeLabel,
        PeriodPhase phase,
        int unreadCount,
        List<TagDto> tags,
        TagDto assignedTeacherTag
) {

    public static ConversationDto from(Conversation c, String timeLabel) {
        return new ConversationDto(
                c.getId(),
                c.getType(),
                UserDto.from(c.getStudent()),
                c.getLastMessagePreview(),
                c.getLastMessageAt(),
                timeLabel,
                c.getPhase(),
                c.getUnreadCount(),
                c.getTags().stream().map(TagDto::from).toList(),
                c.getAssignedTeacherTag() != null ? TagDto.from(c.getAssignedTeacherTag()) : null
        );
    }
}
