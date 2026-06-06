package com.wechat.hosting.repository;
import com.wechat.hosting.entity.ConversationHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ConversationHandlerRepository extends JpaRepository<ConversationHandler, Long> {
    Optional<ConversationHandler> findByConversationId(Long conversationId);
    List<ConversationHandler> findByHandlerUserId(Long handlerUserId);
    List<ConversationHandler> findByHostingAssignmentId(Long hostingAssignmentId);
}
