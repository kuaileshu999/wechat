package com.edu.chat.dto;

import com.edu.chat.model.User;
import com.edu.chat.model.UserRole;

public record UserDto(Long id, String name, String avatar, UserRole role, boolean online) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getAvatar(),
                user.getRole(),
                user.isOnline()
        );
    }
}
