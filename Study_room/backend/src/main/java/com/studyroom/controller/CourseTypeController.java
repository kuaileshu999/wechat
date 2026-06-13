package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.entity.CourseType;
import com.studyroom.service.CourseTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-types")
@RequiredArgsConstructor
public class CourseTypeController {

    private final CourseTypeService courseTypeService;

    @GetMapping
    public ApiResponse<PageResult<CourseType>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(courseTypeService.list(page, size));
    }

    @GetMapping("/enabled/{campusId}")
    public ApiResponse<List<CourseType>> listEnabled(@PathVariable Long campusId) {
        return ApiResponse.success(courseTypeService.listEnabledByCampus(campusId));
    }

    @PostMapping
    public ApiResponse<CourseType> create(@RequestBody CourseType courseType) {
        return ApiResponse.success(courseTypeService.create(courseType));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<CourseType> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.success(courseTypeService.updateStatus(id, status));
    }
}
