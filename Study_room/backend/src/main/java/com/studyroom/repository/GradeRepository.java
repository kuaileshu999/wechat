package com.studyroom.repository;

import com.studyroom.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStatus(Integer status);

    Optional<Grade> findByName(String name);
}
