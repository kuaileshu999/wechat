package com.edu.chat.service;

import com.edu.chat.dto.*;
import com.edu.chat.model.*;
import com.edu.chat.repository.*;
import com.edu.chat.util.TimeLabelUtil;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Long DEFAULT_AGENT_ID = 1L;
    private static final Pattern MATH_PATTERN = Pattern.compile("\\$[^$]+\\$|\\\\\\(|\\\\\\[");

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TagRepository tagRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            TagRepository tagRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.tagRepository = tagRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public AgentProfileDto getAgentProfile() {
        User agent = userRepository.findById(DEFAULT_AGENT_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));
        int conversion = (int) conversationRepository.findAll().stream()
                .filter(c -> c.getPhase() == PeriodPhase.CONVERSION).count();
        int undertaking = (int) conversationRepository.findAll().stream()
                .filter(c -> c.getPhase() == PeriodPhase.UNDERTAKING).count();
        return new AgentProfileDto(
                UserDto.from(agent),
                "接管中",
                5,
                conversationRepository.totalUnread(),
                conversion,
                undertaking
        );
    }

    public List<TagDto> listTags() {
        return tagRepository.findAll().stream().map(TagDto::from).toList();
    }

    public List<ConversationDto> listConversations(
            PeriodPhase phase,
            ConversationType type,
            Long tagId,
            String keyword) {
        List<Conversation> list = tagId == null
                ? conversationRepository.findByPhaseAndTypeOrderByLastMessageAtDesc(phase, type)
                : conversationRepository.findFiltered(phase, type, tagId);

        return list.stream()
                .filter(c -> matchesKeyword(c, keyword))
                .map(c -> ConversationDto.from(c, TimeLabelUtil.relative(c.getLastMessageAt())))
                .toList();
    }

    private boolean matchesKeyword(Conversation c, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.trim().toLowerCase();
        return c.getStudent().getName().toLowerCase().contains(k)
                || (c.getLastMessagePreview() != null
                && c.getLastMessagePreview().toLowerCase().contains(k));
    }

    public ConversationDto getConversation(Long id) {
        Conversation c = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ConversationDto.from(c, TimeLabelUtil.relative(c.getLastMessageAt()));
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(Long conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream()
                .map(m -> MessageDto.from(m, DEFAULT_AGENT_ID, TimeLabelUtil.messageTime(m.getSentAt())))
                .toList();
    }

    @Transactional
    public MessageDto sendMessage(SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean containsMath = MATH_PATTERN.matcher(request.content()).find();
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .sentAt(Instant.now())
                .containsMath(containsMath)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessagePreview(truncate(request.content()));
        conversation.setLastMessageAt(message.getSentAt());
        if (sender.getRole() == UserRole.STUDENT) {
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        } else {
            conversation.setUnreadCount(0);
        }
        conversationRepository.save(conversation);

        MessageDto dto = MessageDto.from(message, DEFAULT_AGENT_ID, TimeLabelUtil.messageTime(message.getSentAt()));
        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), dto);
        messagingTemplate.convertAndSend("/topic/conversations", "refresh");
        return dto;
    }

    @Transactional
    public ConversationDto markRead(Long conversationId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setUnreadCount(0);
        conversationRepository.save(c);
        ConversationDto dto = ConversationDto.from(c, TimeLabelUtil.relative(c.getLastMessageAt()));
        messagingTemplate.convertAndSend("/topic/conversations", "refresh");
        return dto;
    }

    @Transactional
    public ConversationDto transfer(Long conversationId, TransferRequest request) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Tag tag = tagRepository.findById(request.targetTeacherTagId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setAssignedTeacherTag(tag);
        conversationRepository.save(c);
        return ConversationDto.from(c, TimeLabelUtil.relative(c.getLastMessageAt()));
    }

    @Transactional
    public ConversationDto updatePhase(Long conversationId, PeriodPhase phase) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setPhase(phase);
        conversationRepository.save(c);
        return ConversationDto.from(c, TimeLabelUtil.relative(c.getLastMessageAt()));
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        String plain = content.replaceAll("\\$[^$]+\\$", "[公式]");
        return plain.length() > 40 ? plain.substring(0, 40) + "…" : plain;
    }
}
