package com.wechat.hosting.service;

import com.wechat.hosting.dto.MessageVO;
import com.wechat.hosting.dto.ReplyRequest;
import com.wechat.hosting.entity.Conversation;
import com.wechat.hosting.entity.ConversationHandler;
import com.wechat.hosting.entity.Message;
import com.wechat.hosting.entity.SysUser;
import com.wechat.hosting.repository.ConversationHandlerRepository;
import com.wechat.hosting.repository.ConversationRepository;
import com.wechat.hosting.repository.MessageRepository;
import com.wechat.hosting.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationHandlerRepository conversationHandlerRepository;
    private final SysUserRepository sysUserRepository;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          ConversationHandlerRepository conversationHandlerRepository,
                          SysUserRepository sysUserRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationHandlerRepository = conversationHandlerRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public List<MessageVO> listByConversation(Long conversationId) {
        requireConversation(conversationId);
        return messageRepository.findTop50ByConversationIdOrderBySentAtAsc(conversationId)
                .stream().map(this::toVO).toList();
    }

    @Transactional
    public MessageVO reply(ReplyRequest request) {
        Conversation conversation = requireConversation(request.conversationId());
        ConversationHandler handler = conversationHandlerRepository
                .findByConversationId(request.conversationId())
                .orElseThrow(() -> new IllegalArgumentException("会话尚未分配处理人"));
        if (!handler.getHandlerUserId().equals(request.senderUserId())) {
            throw new IllegalArgumentException("当前用户无权回复该会话");
        }
        SysUser sender = sysUserRepository.findById(request.senderUserId())
                .orElseThrow(() -> new IllegalArgumentException("发送者不存在"));

        Message message = new Message();
        message.setConversationId(request.conversationId());
        message.setMsgId("local-" + UUID.randomUUID());
        message.setSenderType(2);
        message.setSenderId(request.senderUserId());
        message.setContentType(1);
        message.setContent(request.content());
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageAt(message.getSentAt());
        conversation.setLastMessagePreview(truncate(request.content(), 256));
        conversationRepository.save(conversation);

        return toVO(message, sender.getRealName());
    }

    private Conversation requireConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
    }

    private MessageVO toVO(Message message) {
        String senderName = "";
        if (message.getSenderType() == 1 && message.getSenderId() != null) {
            senderName = "学生";
        } else if (message.getSenderType() == 2 && message.getSenderId() != null) {
            senderName = sysUserRepository.findById(message.getSenderId())
                    .map(SysUser::getRealName).orElse("老师");
        }
        return toVO(message, senderName);
    }

    private MessageVO toVO(Message message, String senderName) {
        return new MessageVO(
                message.getId(),
                message.getConversationId(),
                message.getSenderType(),
                message.getSenderId(),
                senderName,
                message.getContentType(),
                message.getContent(),
                message.getMediaUrl(),
                message.getSentAt()
        );
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
