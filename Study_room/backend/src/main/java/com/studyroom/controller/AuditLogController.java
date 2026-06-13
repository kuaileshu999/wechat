package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.entity.AuditLog;
import com.studyroom.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<List<AuditLog>> getLogs(@RequestParam String entityType,
                                               @RequestParam Long entityId) {
        return ApiResponse.success(auditLogService.getLogs(entityType, entityId));
    }
}
