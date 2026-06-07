package com.wechat.hosting.repository;

import com.wechat.hosting.entity.HostingConfigAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostingConfigAccountRepository extends JpaRepository<HostingConfigAccount, Long> {
    List<HostingConfigAccount> findByHostingConfigId(Long hostingConfigId);
}
