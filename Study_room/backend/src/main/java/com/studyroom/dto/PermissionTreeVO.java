package com.studyroom.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Integer type;
    private List<PermissionTreeVO> children = new ArrayList<>();
}
