package com.wechat.hosting.dto;

public record HostingAssignmentStatsVO(
        long activeCount,
        long tutorCount,
        long managerCount,
        long todayMessageCount
) {
}
