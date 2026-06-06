package com.wechat.hosting.repository;
import com.wechat.hosting.entity.TeachingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeachingGroupRepository extends JpaRepository<TeachingGroup, Long> {
    List<TeachingGroup> findByStatusOrderBySortOrderAsc(Integer status);
}
