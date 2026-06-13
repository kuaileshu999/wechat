package com.studyroom.repository;

import com.studyroom.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByCampusIdIn(List<Long> campusIds, Pageable pageable);
    List<Course> findByCampusIdAndStatus(Long campusId, Integer status);
    List<Course> findByCampusIdAndCourseTypeIdAndStatus(Long campusId, Long courseTypeId, Integer status);

    @Query("SELECT c FROM Course c WHERE c.campusId = :campusId AND c.status = 1 "
            + "AND c.name LIKE %:keyword% ORDER BY c.name")
    List<Course> searchEnabledByCampusAndName(@Param("campusId") Long campusId, @Param("keyword") String keyword);
}
