package com.wechat.hosting.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_group")
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_account_id", nullable = false)
    private Long tutorAccountId;

    @Column(name = "group_name", nullable = false, length = 128)
    private String groupName;

    @Column(name = "wechat_chat_id", nullable = false, unique = true, length = 64)
    private String wechatChatId;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(nullable = false)
    private Integer status = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTutorAccountId() { return tutorAccountId; }
    public void setTutorAccountId(Long tutorAccountId) { this.tutorAccountId = tutorAccountId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getWechatChatId() { return wechatChatId; }
    public void setWechatChatId(String wechatChatId) { this.wechatChatId = wechatChatId; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
