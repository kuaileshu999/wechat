package com.studyroom.repository;

import com.studyroom.entity.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {
    Page<CourseType> findByCampusIdIn(List<Long> campusIds, Pageable pageable);
    List<CourseType> findByCampusIdAndStatus(Long campusId, Integer status);
    List<CourseType> findByCampusIdAndNameAndStatus(Long campusId, String name, Integer status);
}
