package com.wechat.hosting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TutorVO(
        Long id,
        Long userId,
        String name,
        Long teachingGroupId,
        String teachingGroupName,
        Integer accountCount,
        List<TutorAccountVO> accounts
) {
}
