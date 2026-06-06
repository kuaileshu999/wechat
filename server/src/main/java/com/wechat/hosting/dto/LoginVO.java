package com.wechat.hosting.dto;

public record LoginVO(
        Long userId,
        String username,
        String realName,
        Integer role,
        String avatarUrl
) {
}
