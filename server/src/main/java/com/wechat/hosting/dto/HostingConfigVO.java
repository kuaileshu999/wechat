package com.wechat.hosting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HostingConfigVO(
        Long id,
        Long takeoverManagerId,
        String takeoverManagerName,
        Integer effectiveType,
        LocalDateTime scheduledStartAt,
        String description,
        Integer status,
        Long createdBy,
        LocalDateTime createdAt,
        List<HostingConfigTutorVO> tutors
) {
}
