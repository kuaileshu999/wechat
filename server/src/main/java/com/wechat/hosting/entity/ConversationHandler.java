package com.wechat.hosting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_handler")
public class ConversationHandler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, unique = true)
    private Long conversationId;

    @Column(name = "handler_user_id", nullable = false)
    private Long handlerUserId;

    @Column(name = "handler_type", nullable = false)
    private Integer handlerType = 1;

    @Column(name = "hosting_assignment_id")
    private Long hostingAssignmentId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (assignedAt == null) {
            assignedAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getHandlerUserId() { return handlerUserId; }
    public void setHandlerUserId(Long handlerUserId) { this.handlerUserId = handlerUserId; }
    public Integer getHandlerType() { return handlerType; }
    public void setHandlerType(Integer handlerType) { this.handlerType = handlerType; }
    public Long getHostingAssignmentId() { return hostingAssignmentId; }
    public void setHostingAssignmentId(Long hostingAssignmentId) { this.hostingAssignmentId = hostingAssignmentId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
