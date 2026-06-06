package com.chat2.takeover.controller;

import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.ConversationQuery;
import com.chat2.takeover.dto.ConversationVO;
import com.chat2.takeover.dto.PageResult;
import com.chat2.takeover.dto.UnrepliedStatsVO;
import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;
import com.chat2.takeover.service.ChatMessageService;
import com.chat2.takeover.service.ConversationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final ChatMessageService chatMessageService;

    public ConversationController(ConversationService conversationService, ChatMessageService chatMessageService) {
        this.conversationService = conversationService;
        this.chatMessageService = chatMessageService;
    }

    @GetMapping
    public ApiResponse<PageResult<ConversationVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String chatType,
            @RequestParam(required = false) Long wecomAccountId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        ConversationQuery query = new ConversationQuery();
        if (category != null && !category.isBlank()) {
            query.setCategory(MessageCategory.valueOf(category));
        }
        if (chatType != null && !chatType.isBlank()) {
            query.setChatType(ChatType.valueOf(chatType));
        }
        query.setWecomAccountId(wecomAccountId);
        query.setKeyword(keyword);
        return ApiResponse.ok(conversationService.listPage(query, page, pageSize));
    }

    @GetMapping("/unreplied-counts")
    public ApiResponse<UnrepliedStatsVO> unrepliedCounts() {
        return ApiResponse.ok(conversationService.countAllUnreplied());
    }

    @GetMapping("/{id}")
    public ApiResponse<ConversationVO> detail(@PathVariable Long id) {
        chatMessageService.markRead(id);
        return ApiResponse.ok(conversationService.getDetail(id));
    }
}
