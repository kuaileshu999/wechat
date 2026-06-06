package com.wechat.hosting.dto;

public record TakeoverManagerVO(
        Long id,
        Long userId,
        String name,
        Integer maxTutorCount,
        Integer activeTutorCount,
        Integer status
) {
}
