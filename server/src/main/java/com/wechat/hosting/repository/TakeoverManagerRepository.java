package com.wechat.hosting.repository;
import com.wechat.hosting.entity.TakeoverManager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TakeoverManagerRepository extends JpaRepository<TakeoverManager, Long> {
    List<TakeoverManager> findByStatus(Integer status);
    Optional<TakeoverManager> findByUserId(Long userId);
    long countByStatus(Integer status);
}
