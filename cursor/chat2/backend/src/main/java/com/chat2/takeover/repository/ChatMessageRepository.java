package com.chat2.takeover.repository;

import com.chat2.takeover.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderBySentAtAsc(Long conversationId);

    List<ChatMessage> findTop50ByConversationIdOrderBySentAtDesc(Long conversationId);
}
