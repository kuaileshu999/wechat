package com.chat2.takeover.controller;

import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.ChatMessageVO;
import com.chat2.takeover.dto.ReplyRequest;
import com.chat2.takeover.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final ChatMessageService chatMessageService;

    public MessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping
    public ApiResponse<List<ChatMessageVO>> list(@RequestParam Long conversationId) {
        return ApiResponse.ok(chatMessageService.listByConversation(conversationId));
    }

    @PostMapping("/reply")
    public ApiResponse<ChatMessageVO> reply(@Valid @RequestBody ReplyRequest request) {
        return ApiResponse.ok(chatMessageService.reply(request));
    }
}
