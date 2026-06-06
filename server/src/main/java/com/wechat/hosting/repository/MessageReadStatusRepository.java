package com.wechat.hosting.repository;
import com.wechat.hosting.entity.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {
    Optional<MessageReadStatus> findByConversationIdAndReaderUserId(Long conversationId, Long readerUserId);
    List<MessageReadStatus> findByReaderUserId(Long readerUserId);
    @Query("SELECT COALESCE(SUM(m.unreadCount), 0) FROM MessageReadStatus m WHERE m.readerUserId = :userId")
    long sumUnreadByReaderUserId(@Param("userId") Long userId);
}
