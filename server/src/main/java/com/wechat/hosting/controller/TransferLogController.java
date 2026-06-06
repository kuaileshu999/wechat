package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.TransferLogVO;
import com.wechat.hosting.service.TransferLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-logs")
public class TransferLogController {

    private final TransferLogService transferLogService;

    public TransferLogController(TransferLogService transferLogService) {
        this.transferLogService = transferLogService;
    }

    @GetMapping
    public ApiResponse<List<TransferLogVO>> list(@RequestParam(required = false) Long tutorId) {
        if (tutorId != null) {
            return ApiResponse.ok(transferLogService.listByTutor(tutorId));
        }
        return ApiResponse.ok(transferLogService.listRecent());
    }
}
