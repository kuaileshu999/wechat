package com.chat2.takeover.repository;

import com.chat2.takeover.entity.OrgDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgDepartmentRepository extends JpaRepository<OrgDepartment, Long> {

    List<OrgDepartment> findAllByOrderBySortOrderAscIdAsc();
}
