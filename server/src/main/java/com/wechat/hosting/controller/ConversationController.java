package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.common.PageResult;
import com.wechat.hosting.dto.ConversationVO;
import com.wechat.hosting.dto.TransferConversationRequest;
import com.wechat.hosting.dto.WorkbenchStatsVO;
import com.wechat.hosting.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ApiResponse<PageResult<ConversationVO>> list(
            @RequestParam(required = false) Long handlerUserId,
            @RequestParam(required = false) Integer stage,
            @RequestParam(required = false) Integer convType,
            @RequestParam(required = false) Long tutorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(conversationService.list(handlerUserId, stage, convType, tutorId, keyword, page, pageSize));
    }

    @GetMapping("/stats")
    public ApiResponse<WorkbenchStatsVO> stats(@RequestParam Long handlerUserId) {
        return ApiResponse.ok(conversationService.stats(handlerUserId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConversationVO> detail(@PathVariable Long id,
                                              @RequestParam(required = false) Long readerUserId) {
        return ApiResponse.ok(conversationService.getDetail(id, readerUserId));
    }

    @PostMapping("/{id}/transfer")
    public ApiResponse<ConversationVO> transfer(@PathVariable Long id,
                                                @Valid @RequestBody TransferConversationRequest request) {
        return ApiResponse.ok(conversationService.transfer(id, request));
    }
}
