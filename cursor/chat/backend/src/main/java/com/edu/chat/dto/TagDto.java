package com.edu.chat.dto;

import com.edu.chat.model.Tag;

public record TagDto(Long id, String name, String color) {

    public static TagDto from(Tag tag) {
        return new TagDto(tag.getId(), tag.getName(), tag.getColor());
    }
}
