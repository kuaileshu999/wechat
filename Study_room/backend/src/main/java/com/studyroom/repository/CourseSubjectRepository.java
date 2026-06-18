package com.studyroom.repository;

import com.studyroom.entity.CourseSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CourseSubjectRepository extends JpaRepository<CourseSubject, Long> {

    List<CourseSubject> findByCourseId(Long courseId);

    List<CourseSubject> findByCourseIdIn(Collection<Long> courseIds);

    void deleteByCourseId(Long courseId);
}
