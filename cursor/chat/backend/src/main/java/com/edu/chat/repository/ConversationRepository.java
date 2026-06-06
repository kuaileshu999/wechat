package com.edu.chat.repository;

import com.edu.chat.model.Conversation;
import com.edu.chat.model.ConversationType;
import com.edu.chat.model.PeriodPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByPhaseAndTypeOrderByLastMessageAtDesc(PeriodPhase phase, ConversationType type);

    @Query("""
            SELECT DISTINCT c FROM Conversation c
            LEFT JOIN c.tags t
            WHERE c.phase = :phase AND c.type = :type
            AND (:tagId IS NULL OR t.id = :tagId OR c.assignedTeacherTag.id = :tagId)
            ORDER BY c.lastMessageAt DESC
            """)
    List<Conversation> findFiltered(
            @Param("phase") PeriodPhase phase,
            @Param("type") ConversationType type,
            @Param("tagId") Long tagId);

    @Query("SELECT COALESCE(SUM(c.unreadCount), 0) FROM Conversation c")
    int totalUnread();
}
