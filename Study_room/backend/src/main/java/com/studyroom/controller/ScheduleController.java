package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.ScheduleBatchCreateRequest;
import com.studyroom.dto.ScheduleBatchDeleteRequest;
import com.studyroom.dto.ScheduleRequest;
import com.studyroom.dto.ScheduleVO;
import com.studyroom.enums.ScheduleStatus;
import com.studyroom.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ApiResponse<PageResult<ScheduleVO>> list(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long courseTypeId,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(scheduleService.list(campusId, teacherId, courseTypeId, status,
                startDate, endDate, page, size));
    }

    @GetMapping("/my")
    public ApiResponse<List<ScheduleVO>> listMy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long courseTypeId) {
        return ApiResponse.success(scheduleService.listMy(startDate, endDate, courseTypeId));
    }

    @PostMapping
    public ApiResponse<ScheduleVO> create(@Valid @RequestBody ScheduleRequest request) {
        return ApiResponse.success(scheduleService.create(request));
    }

    @PostMapping("/batch")
    public ApiResponse<Integer> batchCreate(@Valid @RequestBody ScheduleBatchCreateRequest request) {
        return ApiResponse.success(scheduleService.batchCreate(request));
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importSchedules(@RequestParam Long campusId,
                                                @RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.success(scheduleService.importSchedules(file, campusId));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScheduleVO> update(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        return ApiResponse.success(scheduleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Integer> batchDelete(@Valid @RequestBody ScheduleBatchDeleteRequest request) {
        return ApiResponse.success(scheduleService.batchDelete(request));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<ScheduleVO> complete(@PathVariable Long id) {
        return ApiResponse.success(scheduleService.complete(id));
    }
}
