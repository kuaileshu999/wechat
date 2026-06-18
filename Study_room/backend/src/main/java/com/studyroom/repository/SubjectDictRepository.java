package com.studyroom.repository;

import com.studyroom.entity.SubjectDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectDictRepository extends JpaRepository<SubjectDict, Long> {
    List<SubjectDict> findByStatus(Integer status);

    Optional<SubjectDict> findByName(String name);
}
