package com.studyroom.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private String name;
    private String code;
    private String path;
    private String icon;
    private List<MenuVO> children = new ArrayList<>();
}
