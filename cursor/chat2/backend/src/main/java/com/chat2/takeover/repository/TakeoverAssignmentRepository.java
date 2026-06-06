package com.chat2.takeover.repository;

import com.chat2.takeover.entity.TakeoverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TakeoverAssignmentRepository extends JpaRepository<TakeoverAssignment, Long> {

    List<TakeoverAssignment> findAllByOrderByIdDesc();

    Optional<TakeoverAssignment> findBySourceTutorIdAndWecomAccountIdAndTakeoverTutorId(
            Long sourceTutorId, Long wecomAccountId, Long takeoverTutorId);
}
