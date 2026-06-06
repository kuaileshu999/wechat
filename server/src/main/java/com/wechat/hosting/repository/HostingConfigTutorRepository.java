package com.wechat.hosting.repository;
import com.wechat.hosting.entity.HostingConfigTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HostingConfigTutorRepository extends JpaRepository<HostingConfigTutor, Long> {
    List<HostingConfigTutor> findByHostingConfigId(Long hostingConfigId);
}
