package com.chat2.takeover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_account")
public class WecomAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;
    @Column(name = "account_name", nullable = false, length = 128)
    private String accountName;
    @Column(name = "wecom_user_id", length = 64)
    private String wecomUserId;
    @Column(name = "grade_level", length = 32)
    private String gradeLevel;
    @Column(name = "subject", length = 32)
    private String subject;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getWecomUserId() { return wecomUserId; }
    public void setWecomUserId(String wecomUserId) { this.wecomUserId = wecomUserId; }
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
