package com.wechat.hosting.repository;
import com.wechat.hosting.entity.HostingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface HostingAssignmentRepository extends JpaRepository<HostingAssignment, Long> {
    Optional<HostingAssignment> findByTutorIdAndStatus(Long tutorId, Integer status);
    List<HostingAssignment> findByTakeoverManagerIdAndStatus(Long takeoverManagerId, Integer status);
    boolean existsByTutorIdAndStatus(Long tutorId, Integer status);
}
