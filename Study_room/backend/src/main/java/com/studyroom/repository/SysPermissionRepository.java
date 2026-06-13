package com.studyroom.repository;

import com.studyroom.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    List<SysPermission> findByTypeOrderBySortOrderAsc(Integer type);

    List<SysPermission> findAllByOrderBySortOrderAsc();
}
