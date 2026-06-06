package com.edu.chat.controller;

import com.edu.chat.dto.*;
import com.edu.chat.model.ConversationType;
import com.edu.chat.model.PeriodPhase;
import com.edu.chat.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/profile")
    public AgentProfileDto profile() {
        return chatService.getAgentProfile();
    }

    @GetMapping("/tags")
    public List<TagDto> tags() {
        return chatService.listTags();
    }

    @GetMapping("/conversations")
    public List<ConversationDto> conversations(
            @RequestParam(defaultValue = "CONVERSION") PeriodPhase phase,
            @RequestParam(defaultValue = "PRIVATE") ConversationType type,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return chatService.listConversations(phase, type, tagId, keyword);
    }

    @GetMapping("/conversations/{id}")
    public ConversationDto conversation(@PathVariable Long id) {
        return chatService.getConversation(id);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDto> messages(@PathVariable Long id) {
        return chatService.getMessages(id);
    }

    @PostMapping("/messages")
    public MessageDto send(@RequestBody SendMessageRequest request) {
        return chatService.sendMessage(request);
    }

    @PutMapping("/conversations/{id}/read")
    public ConversationDto markRead(@PathVariable Long id) {
        return chatService.markRead(id);
    }

    @PutMapping("/conversations/{id}/transfer")
    public ConversationDto transfer(@PathVariable Long id, @RequestBody TransferRequest request) {
        return chatService.transfer(id, request);
    }

    @PutMapping("/conversations/{id}/phase")
    public ConversationDto updatePhase(@PathVariable Long id, @RequestParam PeriodPhase phase) {
        return chatService.updatePhase(id, phase);
    }
}
