package com.wechat.hosting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hosting_config")
public class HostingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "takeover_manager_id", nullable = false)
    private Long takeoverManagerId;

    @Column(name = "effective_type", nullable = false)
    private Integer effectiveType = 1;

    @Column(name = "scheduled_start_at")
    private LocalDateTime scheduledStartAt;

    @Column(length = 512)
    private String description;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

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
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTakeoverManagerId() { return takeoverManagerId; }
    public void setTakeoverManagerId(Long takeoverManagerId) { this.takeoverManagerId = takeoverManagerId; }
    public Integer getEffectiveType() { return effectiveType; }
    public void setEffectiveType(Integer effectiveType) { this.effectiveType = effectiveType; }
    public LocalDateTime getScheduledStartAt() { return scheduledStartAt; }
    public void setScheduledStartAt(LocalDateTime scheduledStartAt) { this.scheduledStartAt = scheduledStartAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
