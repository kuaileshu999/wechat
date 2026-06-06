package com.wechat.hosting.dto;

public record HostingConfigTutorVO(
        Long tutorId,
        String tutorName,
        String teachingGroupName,
        Integer status,
        String skipReason
) {
}
