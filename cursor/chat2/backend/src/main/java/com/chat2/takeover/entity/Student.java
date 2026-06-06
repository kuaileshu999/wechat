package com.chat2.takeover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "wecom_account_id", nullable = false)
    private Long wecomAccountId;
    @Column(nullable = false, length = 64)
    private String name;
    @Column(name = "external_user_id", length = 64)
    private String externalUserId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWecomAccountId() { return wecomAccountId; }
    public void setWecomAccountId(Long wecomAccountId) { this.wecomAccountId = wecomAccountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getExternalUserId() { return externalUserId; }
    public void setExternalUserId(String externalUserId) { this.externalUserId = externalUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
