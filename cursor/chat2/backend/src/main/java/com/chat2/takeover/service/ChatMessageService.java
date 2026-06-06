package com.chat2.takeover.service;

import com.chat2.takeover.dto.ChatMessageVO;
import com.chat2.takeover.dto.ReplyRequest;
import com.chat2.takeover.entity.ChatMessage;
import com.chat2.takeover.entity.Conversation;
import com.chat2.takeover.enums.MessageDirection;
import com.chat2.takeover.repository.ChatMessageRepository;
import com.chat2.takeover.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            ConversationRepository conversationRepository,
            ConversationService conversationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
    }

    public List<ChatMessageVO> listByConversation(Long conversationId) {
        conversationService.require(conversationId);
        List<ChatMessage> recent = chatMessageRepository
                .findTop50ByConversationIdOrderBySentAtDesc(conversationId);
        List<ChatMessage> ordered = new ArrayList<>(recent);
        Collections.reverse(ordered);
        return ordered.stream().map(this::toVO).toList();
    }

    @Transactional
    public ChatMessageVO reply(ReplyRequest request) {
        Conversation conversation = conversationService.require(request.conversationId());

        ChatMessage message = new ChatMessage();
        message.setConversationId(request.conversationId());
        message.setDirection(MessageDirection.OUTGOING);
        message.setSenderName(request.senderName());
        message.setContent(request.content().trim());
        message.setSentAt(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);

        conversation.setLastMessagePreview(request.content().trim());
        conversation.setLastMessageAt(saved.getSentAt());
        conversation.setUnreadCount(0);
        conversation.setPendingReply(false);
        conversation.setPendingReplyCount(0);
        conversationRepository.save(conversation);

        return toVO(saved);
    }

    @Transactional
    public void markRead(Long conversationId) {
        Conversation conversation = conversationService.require(conversationId);
        conversation.setUnreadCount(0);
        conversation.setPendingReply(false);
        conversation.setPendingReplyCount(0);
        conversationRepository.save(conversation);
    }

    private ChatMessageVO toVO(ChatMessage m) {
        return new ChatMessageVO(
                m.getId(),
                m.getConversationId(),
                m.getDirection(),
                m.getSenderName(),
                m.getContent(),
                m.getSentAt());
    }
}
