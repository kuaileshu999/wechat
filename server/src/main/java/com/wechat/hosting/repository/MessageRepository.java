package com.wechat.hosting.repository;
import com.wechat.hosting.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop50ByConversationIdOrderBySentAtAsc(Long conversationId);
    @Modifying
    @Query("DELETE FROM Message m WHERE m.sentAt < :before")
    int deleteBySentAtBefore(@Param("before") LocalDateTime before);

    long countBySentAtBetween(LocalDateTime start, LocalDateTime end);
}
