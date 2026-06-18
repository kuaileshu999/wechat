package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.StudentCreateRequest;
import com.studyroom.dto.StudentUpdateRequest;
import com.studyroom.entity.Student;
import com.studyroom.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ApiResponse<PageResult<Student>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(studentService.list(name, page, size));
    }

    @GetMapping("/search")
    public ApiResponse<List<Student>> search(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long campusId) {
        return ApiResponse.success(studentService.search(keyword, campusId));
    }

    @GetMapping("/options")
    public ApiResponse<List<Student>> options(@RequestParam Long campusId,
                                              @RequestParam(required = false) String keyword) {
        return ApiResponse.success(studentService.listOptions(campusId, keyword));
    }

    @GetMapping("/search-for-schedule")
    public ApiResponse<List<Student>> searchForSchedule(@RequestParam String keyword,
                                                      @RequestParam Long campusId) {
        return ApiResponse.success(studentService.searchForSchedule(keyword, campusId));
    }

    @PostMapping
    public ApiResponse<Student> create(@Valid @RequestBody StudentCreateRequest request) {
        return ApiResponse.success(studentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ApiResponse.success(studentService.update(id, request));
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importStudents(@RequestParam Long campusId,
                                               @RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.success(studentService.importStudents(file, campusId));
    }
}
