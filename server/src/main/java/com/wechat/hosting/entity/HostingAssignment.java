package com.wechat.hosting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hosting_assignment")
public class HostingAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hosting_config_id", nullable = false)
    private Long hostingConfigId;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "tutor_account_id")
    private Long tutorAccountId;

    @Column(name = "takeover_manager_id", nullable = false)
    private Long takeoverManagerId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = now;
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
    public Long getHostingConfigId() { return hostingConfigId; }
    public void setHostingConfigId(Long hostingConfigId) { this.hostingConfigId = hostingConfigId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public Long getTutorAccountId() { return tutorAccountId; }
    public void setTutorAccountId(Long tutorAccountId) { this.tutorAccountId = tutorAccountId; }
    public Long getTakeoverManagerId() { return takeoverManagerId; }
    public void setTakeoverManagerId(Long takeoverManagerId) { this.takeoverManagerId = takeoverManagerId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
