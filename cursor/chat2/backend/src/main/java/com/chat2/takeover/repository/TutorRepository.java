package com.chat2.takeover.repository;

import com.chat2.takeover.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    List<Tutor> findByTakeoverRoleTrue();

    List<Tutor> findByTakeoverRoleFalseOrderByIdAsc();

    List<Tutor> findByOrgDepartmentIdIn(Collection<Long> orgDepartmentIds);
}
