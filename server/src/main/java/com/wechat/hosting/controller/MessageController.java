package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.MessageVO;
import com.wechat.hosting.dto.ReplyRequest;
import com.wechat.hosting.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<MessageVO>> list(@RequestParam Long conversationId) {
        return ApiResponse.ok(messageService.listByConversation(conversationId));
    }

    @PostMapping("/reply")
    public ApiResponse<MessageVO> reply(@Valid @RequestBody ReplyRequest request) {
        return ApiResponse.ok(messageService.reply(request));
    }
}
