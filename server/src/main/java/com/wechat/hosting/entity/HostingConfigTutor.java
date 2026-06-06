package com.wechat.hosting.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hosting_config_tutor")
public class HostingConfigTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hosting_config_id", nullable = false)
    private Long hostingConfigId;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "skip_reason", length = 128)
    private String skipReason;

    @Column(nullable = false)
    private Integer status = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHostingConfigId() { return hostingConfigId; }
    public void setHostingConfigId(Long hostingConfigId) { this.hostingConfigId = hostingConfigId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getSkipReason() { return skipReason; }
    public void setSkipReason(String skipReason) { this.skipReason = skipReason; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
