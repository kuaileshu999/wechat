package com.chat2.takeover.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BatchTakeoverRequest(
        @NotNull Long takeoverTutorId,
        @NotEmpty List<Long> sourceTutorIds,
        List<Long> wecomAccountIds,
        Boolean assignAllAccounts,
        Boolean enabled) {

    public Boolean assignAllAccounts() {
        return assignAllAccounts != null && assignAllAccounts;
    }

    public Boolean enabled() {
        return enabled != null ? enabled : true;
    }

    public List<Long> wecomAccountIds() {
        return wecomAccountIds != null ? wecomAccountIds : List.of();
    }
}
