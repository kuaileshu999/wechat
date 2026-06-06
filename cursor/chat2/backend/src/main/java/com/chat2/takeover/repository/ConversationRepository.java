package com.chat2.takeover.repository;

import com.chat2.takeover.entity.Conversation;
import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConversationRepository extends JpaRepository<Conversation, Long>, JpaSpecificationExecutor<Conversation> {

    long countByCategoryAndChatTypeAndPendingReply(
            MessageCategory category, ChatType chatType, Boolean pendingReply);
}
