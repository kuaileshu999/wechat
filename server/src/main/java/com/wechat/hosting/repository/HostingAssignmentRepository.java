package com.wechat.hosting.repository;

import com.wechat.hosting.entity.HostingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface HostingAssignmentRepository extends JpaRepository<HostingAssignment, Long>,
        JpaSpecificationExecutor<HostingAssignment> {

    Optional<HostingAssignment> findByTutorIdAndStatus(Long tutorId, Integer status);

    Optional<HostingAssignment> findByTutorAccountIdAndStatus(Long tutorAccountId, Integer status);

    List<HostingAssignment> findByTakeoverManagerIdAndStatus(Long takeoverManagerId, Integer status);

    boolean existsByTutorIdAndStatus(Long tutorId, Integer status);

    boolean existsByTutorAccountIdAndStatus(Long tutorAccountId, Integer status);

    long countByStatus(Integer status);

    List<HostingAssignment> findByStatus(Integer status);
}
