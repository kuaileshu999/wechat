package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.entity.TeachingGroup;
import com.wechat.hosting.service.TeachingGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teaching-groups")
public class TeachingGroupController {

    private final TeachingGroupService teachingGroupService;

    public TeachingGroupController(TeachingGroupService teachingGroupService) {
        this.teachingGroupService = teachingGroupService;
    }

    @GetMapping
    public ApiResponse<List<TeachingGroup>> list() {
        return ApiResponse.ok(teachingGroupService.listActive());
    }
}
