package com.wechat.hosting.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record CreateHostingConfigRequest(
        @NotNull Long takeoverManagerId,
        @NotNull Integer effectiveType,
        LocalDateTime scheduledStartAt,
        String description,
        @NotNull Long createdBy,
        List<Long> tutorIds,
        List<Long> accountIds
) {
}
