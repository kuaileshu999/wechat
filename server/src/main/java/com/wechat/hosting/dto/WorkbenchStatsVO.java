package com.wechat.hosting.dto;

public record WorkbenchStatsVO(
        Integer hostingTutorCount,
        Long totalUnread,
        Integer conversionCount,
        Integer undertakingCount,
        Integer completedCount
) {
}
