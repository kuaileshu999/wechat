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

    @Query("SELECT c FROM Course c WHERE c.campusId IN :campusIds "
            + "AND (:campusId IS NULL OR c.campusId = :campusId) "
            + "AND (:name IS NULL OR c.name LIKE CONCAT('%', :name, '%')) "
            + "AND (:gradeId IS NULL OR c.gradeId = :gradeId) "
            + "AND (:subjectId IS NULL OR c.subjectId = :subjectId "
            + "OR EXISTS (SELECT 1 FROM CourseSubject cs WHERE cs.courseId = c.id AND cs.subjectId = :subjectId)) "
            + "ORDER BY c.id DESC")
    Page<Course> search(@Param("campusIds") List<Long> campusIds,
                        @Param("campusId") Long campusId,
                        @Param("name") String name,
                        @Param("subjectId") Long subjectId,
                        @Param("gradeId") Long gradeId,
                        Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.campusId = :campusId AND c.status = 1 "
            + "AND c.name LIKE %:keyword% ORDER BY c.name")
    List<Course> searchEnabledByCampusAndName(@Param("campusId") Long campusId, @Param("keyword") String keyword);
}
