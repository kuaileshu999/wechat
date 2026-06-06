package com.wechat.hosting.repository;
import com.wechat.hosting.entity.HostingTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HostingTransferLogRepository extends JpaRepository<HostingTransferLog, Long> {
    List<HostingTransferLog> findTop100ByOrderByCreatedAtDesc();
    List<HostingTransferLog> findByTutorIdOrderByCreatedAtDesc(Long tutorId);
}
