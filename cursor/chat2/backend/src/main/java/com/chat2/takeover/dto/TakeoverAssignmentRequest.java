package com.chat2.takeover.dto;

import jakarta.validation.constraints.NotNull;

public record TakeoverAssignmentRequest(
        @NotNull Long sourceTutorId,
        Long wecomAccountId,
        @NotNull Long takeoverTutorId,
        Boolean enabled) {

    public Boolean enabled() {
        return enabled != null ? enabled : true;
    }
}
