package com.chat2.takeover.entity;

import com.chat2.takeover.enums.MessageDirection;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MessageDirection direction;
    @Column(name = "sender_name", length = 64)
    private String senderName;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (sentAt == null) sentAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public MessageDirection getDirection() { return direction; }
    public void setDirection(MessageDirection direction) { this.direction = direction; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
