package com.chat2.takeover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "takeover_assignment")
public class TakeoverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "source_tutor_id", nullable = false)
    private Long sourceTutorId;
    @Column(name = "wecom_account_id")
    private Long wecomAccountId;
    @Column(name = "takeover_tutor_id", nullable = false)
    private Long takeoverTutorId;
    @Column(nullable = false)
    private Boolean enabled = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceTutorId() { return sourceTutorId; }
    public void setSourceTutorId(Long sourceTutorId) { this.sourceTutorId = sourceTutorId; }
    public Long getWecomAccountId() { return wecomAccountId; }
    public void setWecomAccountId(Long wecomAccountId) { this.wecomAccountId = wecomAccountId; }
    public Long getTakeoverTutorId() { return takeoverTutorId; }
    public void setTakeoverTutorId(Long takeoverTutorId) { this.takeoverTutorId = takeoverTutorId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
