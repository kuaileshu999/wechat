package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.entity.Course;
import com.studyroom.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<PageResult<Course>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(courseService.list(page, size));
    }

    @GetMapping("/enabled/{campusId}")
    public ApiResponse<List<Course>> listEnabled(@PathVariable Long campusId) {
        return ApiResponse.success(courseService.listEnabledByCampus(campusId));
    }

    @GetMapping("/search")
    public ApiResponse<List<Course>> searchEnabled(@RequestParam Long campusId,
                                                   @RequestParam String keyword) {
        return ApiResponse.success(courseService.searchEnabled(campusId, keyword));
    }

    @PostMapping
    public ApiResponse<Course> create(@RequestBody Course course) {
        return ApiResponse.success(courseService.create(course));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Course> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.success(courseService.updateStatus(id, status));
    }
}
