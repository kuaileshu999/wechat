package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.dto.PermissionTreeVO;
import com.studyroom.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final RoleService roleService;

    @GetMapping("/tree")
    public ApiResponse<List<PermissionTreeVO>> tree() {
        return ApiResponse.success(roleService.getPermissionTree());
    }
}
