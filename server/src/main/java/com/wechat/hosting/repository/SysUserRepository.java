package com.wechat.hosting.repository;
import com.wechat.hosting.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    Optional<SysUser> findByPhone(String phone);
    List<SysUser> findByIdIn(List<Long> ids);
}
