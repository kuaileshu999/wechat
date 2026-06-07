package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.common.PageResult;
import com.wechat.hosting.dto.HostingAssignmentStatsVO;
import com.wechat.hosting.dto.HostingAssignmentVO;
import com.wechat.hosting.service.HostingAssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hosting-assignments")
public class HostingAssignmentController {

    private final HostingAssignmentService hostingAssignmentService;

    public HostingAssignmentController(HostingAssignmentService hostingAssignmentService) {
        this.hostingAssignmentService = hostingAssignmentService;
    }

    @GetMapping
    public ApiResponse<PageResult<HostingAssignmentVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(hostingAssignmentService.list(keyword, status, accountId, page, pageSize));
    }

    @GetMapping("/stats")
    public ApiResponse<HostingAssignmentStatsVO> stats() {
        return ApiResponse.ok(hostingAssignmentService.stats());
    }

    @PostMapping("/{id}/release")
    public ApiResponse<HostingAssignmentVO> release(@PathVariable Long id,
                                                    @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        return ApiResponse.ok(hostingAssignmentService.release(id, operatorId));
    }

    @PostMapping("/{id}/reactivate")
    public ApiResponse<HostingAssignmentVO> reactivate(@PathVariable Long id,
                                                       @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        return ApiResponse.ok(hostingAssignmentService.reactivate(id, operatorId));
    }
}
