package com.wechat.hosting.dto;

import java.time.LocalDateTime;

public record HostingAssignmentVO(
        Long id,
        Long hostingConfigId,
        Long tutorId,
        String tutorName,
        String teachingGroupName,
        Long tutorAccountId,
        String accountName,
        String wechatUserid,
        Long takeoverManagerId,
        String takeoverManagerName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer status,
        String description,
        Integer effectiveType
) {
}
