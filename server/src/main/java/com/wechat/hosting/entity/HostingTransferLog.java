package com.wechat.hosting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hosting_transfer_log")
public class HostingTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "from_handler_user_id")
    private Long fromHandlerUserId;

    @Column(name = "to_handler_user_id")
    private Long toHandlerUserId;

    @Column(name = "action_type", nullable = false)
    private Integer actionType;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(length = 512)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getFromHandlerUserId() { return fromHandlerUserId; }
    public void setFromHandlerUserId(Long fromHandlerUserId) { this.fromHandlerUserId = fromHandlerUserId; }
    public Long getToHandlerUserId() { return toHandlerUserId; }
    public void setToHandlerUserId(Long toHandlerUserId) { this.toHandlerUserId = toHandlerUserId; }
    public Integer getActionType() { return actionType; }
    public void setActionType(Integer actionType) { this.actionType = actionType; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
