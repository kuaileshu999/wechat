package com.wechat.hosting.repository;
import com.wechat.hosting.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByIdIn(List<Long> ids);
}
