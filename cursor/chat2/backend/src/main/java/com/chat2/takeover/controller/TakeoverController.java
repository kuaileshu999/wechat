package com.chat2.takeover.controller;

import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.BatchTakeoverRequest;
import com.chat2.takeover.dto.BatchTakeoverResultVO;
import com.chat2.takeover.dto.TakeoverAssignmentRequest;
import com.chat2.takeover.dto.TakeoverAssignmentVO;
import com.chat2.takeover.service.TakeoverAssignmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/takeover-assignments")
public class TakeoverController {

    private final TakeoverAssignmentService takeoverAssignmentService;

    public TakeoverController(TakeoverAssignmentService takeoverAssignmentService) {
        this.takeoverAssignmentService = takeoverAssignmentService;
    }

    @GetMapping
    public ApiResponse<List<TakeoverAssignmentVO>> list() {
        return ApiResponse.ok(takeoverAssignmentService.listAll());
    }

    @PostMapping
    public ApiResponse<TakeoverAssignmentVO> save(@Valid @RequestBody TakeoverAssignmentRequest request) {
        return ApiResponse.ok(takeoverAssignmentService.save(request));
    }

    @PostMapping("/batch")
    public ApiResponse<BatchTakeoverResultVO> batchSave(@Valid @RequestBody BatchTakeoverRequest request) {
        return ApiResponse.ok(takeoverAssignmentService.batchSave(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        takeoverAssignmentService.delete(id);
        return ApiResponse.ok(null);
    }
}
