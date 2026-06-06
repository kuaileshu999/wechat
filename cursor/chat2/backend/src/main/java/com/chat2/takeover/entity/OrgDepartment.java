package com.chat2.takeover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_department")
public class OrgDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(nullable = false, length = 64)
    private String name;
    @Column(name = "dept_type", nullable = false, length = 16)
    private String deptType;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeptType() { return deptType; }
    public void setDeptType(String deptType) { this.deptType = deptType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
