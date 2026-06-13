package com.studyroom.repository;

import com.studyroom.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<SysUser> findByUsernameContaining(String username, Pageable pageable);

    Page<SysUser> findByRealNameContaining(String realName, Pageable pageable);

    boolean existsByEmployeeId(Long employeeId);

    Optional<SysUser> findByEmployeeId(Long employeeId);
}
