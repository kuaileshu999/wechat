package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.SubjectUpdateRequest;
import com.studyroom.entity.SubjectDict;
import com.studyroom.service.SubjectDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectDictController {

    private final SubjectDictService subjectDictService;

    @GetMapping
    public ApiResponse<PageResult<SubjectDict>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(subjectDictService.list(page, size));
    }

    @GetMapping("/enabled")
    public ApiResponse<List<SubjectDict>> listEnabled() {
        return ApiResponse.success(subjectDictService.listEnabled());
    }

    @GetMapping("/enabled/{campusId}")
    public ApiResponse<List<SubjectDict>> listEnabledLegacy(@PathVariable Long campusId) {
        return ApiResponse.success(subjectDictService.listEnabled());
    }

    @PostMapping
    public ApiResponse<SubjectDict> create(@RequestBody SubjectDict subject) {
        return ApiResponse.success(subjectDictService.create(subject));
    }

    @PutMapping("/{id}")
    public ApiResponse<SubjectDict> update(@PathVariable Long id, @Valid @RequestBody SubjectUpdateRequest request) {
        return ApiResponse.success(subjectDictService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<SubjectDict> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.success(subjectDictService.updateStatus(id, status));
    }
}
