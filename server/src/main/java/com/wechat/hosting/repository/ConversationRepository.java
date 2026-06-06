package com.wechat.hosting.repository;
import com.wechat.hosting.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
public interface ConversationRepository extends JpaRepository<Conversation, Long>, JpaSpecificationExecutor<Conversation> {
    List<Conversation> findByTutorAccountIdInAndStatus(List<Long> accountIds, Integer status);
}
