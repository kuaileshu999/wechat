package com.wechat.hosting.repository;
import com.wechat.hosting.entity.TutorWechatAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TutorWechatAccountRepository extends JpaRepository<TutorWechatAccount, Long> {
    List<TutorWechatAccount> findByTutorIdAndStatus(Long tutorId, Integer status);
    List<TutorWechatAccount> findByTutorIdInAndStatus(List<Long> tutorIds, Integer status);
}
