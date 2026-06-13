package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.dto.RoleRequest;
import com.studyroom.dto.RoleVO;
import com.studyroom.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<RoleVO>> list() {
        return ApiResponse.success(roleService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleVO> get(@PathVariable Long id) {
        return ApiResponse.success(roleService.get(id));
    }

    @PostMapping
    public ApiResponse<RoleVO> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success(null);
    }
}
