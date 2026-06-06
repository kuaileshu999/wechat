package com.chat2.takeover.entity;

import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "wecom_account_id", nullable = false)
    private Long wecomAccountId;
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false, length = 16)
    private ChatType chatType;
    @Column(name = "group_name", length = 128)
    private String groupName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageCategory category;
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    @Column(name = "last_message_preview", length = 512)
    private String lastMessagePreview;
    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;
    @Column(name = "pending_reply", nullable = false)
    private Boolean pendingReply = false;
    @Column(name = "pending_reply_count", nullable = false)
    private Integer pendingReplyCount = 0;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWecomAccountId() { return wecomAccountId; }
    public void setWecomAccountId(Long wecomAccountId) { this.wecomAccountId = wecomAccountId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public ChatType getChatType() { return chatType; }
    public void setChatType(ChatType chatType) { this.chatType = chatType; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public MessageCategory getCategory() { return category; }
    public void setCategory(MessageCategory category) { this.category = category; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
    public Boolean getPendingReply() { return pendingReply; }
    public void setPendingReply(Boolean pendingReply) { this.pendingReply = pendingReply; }
    public Integer getPendingReplyCount() { return pendingReplyCount; }
    public void setPendingReplyCount(Integer pendingReplyCount) { this.pendingReplyCount = pendingReplyCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
