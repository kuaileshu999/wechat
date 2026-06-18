package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.GradeUpdateRequest;
import com.studyroom.entity.Grade;
import com.studyroom.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ApiResponse<PageResult<Grade>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(gradeService.list(page, size));
    }

    @GetMapping("/enabled")
    public ApiResponse<List<Grade>> listEnabled() {
        return ApiResponse.success(gradeService.listEnabled());
    }

    @GetMapping("/enabled/{campusId}")
    public ApiResponse<List<Grade>> listEnabledLegacy(@PathVariable Long campusId) {
        return ApiResponse.success(gradeService.listEnabled());
    }

    @PostMapping
    public ApiResponse<Grade> create(@RequestBody Grade grade) {
        return ApiResponse.success(gradeService.create(grade));
    }

    @PutMapping("/{id}")
    public ApiResponse<Grade> update(@PathVariable Long id, @Valid @RequestBody GradeUpdateRequest request) {
        return ApiResponse.success(gradeService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Grade> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.success(gradeService.updateStatus(id, status));
    }
}
