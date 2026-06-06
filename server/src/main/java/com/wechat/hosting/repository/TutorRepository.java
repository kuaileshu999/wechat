package com.wechat.hosting.repository;
import com.wechat.hosting.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    List<Tutor> findByStatus(Integer status);
    List<Tutor> findByTeachingGroupIdAndStatus(Long teachingGroupId, Integer status);
    Optional<Tutor> findByUserId(Long userId);
}
