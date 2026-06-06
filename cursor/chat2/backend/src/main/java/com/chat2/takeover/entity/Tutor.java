package com.chat2.takeover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tutor")
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 64)
    private String name;
    @Column(length = 32)
    private String phone;
    @Column(name = "org_department_id")
    private Long orgDepartmentId;
    @Column(name = "is_takeover_role", nullable = false)
    private Boolean takeoverRole = false;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getOrgDepartmentId() { return orgDepartmentId; }
    public void setOrgDepartmentId(Long orgDepartmentId) { this.orgDepartmentId = orgDepartmentId; }
    public Boolean getTakeoverRole() { return takeoverRole; }
    public void setTakeoverRole(Boolean takeoverRole) { this.takeoverRole = takeoverRole; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
