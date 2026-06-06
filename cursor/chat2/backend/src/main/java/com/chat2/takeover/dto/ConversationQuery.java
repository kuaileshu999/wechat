package com.chat2.takeover.dto;

import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;

public class ConversationQuery {
    private MessageCategory category;
    private ChatType chatType;
    private Long tutorId;
    private Long wecomAccountId;
    private String keyword;

    public MessageCategory getCategory() { return category; }
    public void setCategory(MessageCategory category) { this.category = category; }
    public ChatType getChatType() { return chatType; }
    public void setChatType(ChatType chatType) { this.chatType = chatType; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Long getWecomAccountId() { return wecomAccountId; }
    public void setWecomAccountId(Long wecomAccountId) { this.wecomAccountId = wecomAccountId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
