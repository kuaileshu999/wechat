package com.wechat.hosting.repository;
import com.wechat.hosting.entity.HostingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface HostingConfigRepository extends JpaRepository<HostingConfig, Long> {
    List<HostingConfig> findByStatusInOrderByCreatedAtDesc(List<Integer> statuses);
    List<HostingConfig> findByStatusAndEffectiveTypeAndScheduledStartAtLessThanEqual(Integer status, Integer effectiveType, LocalDateTime time);
}
