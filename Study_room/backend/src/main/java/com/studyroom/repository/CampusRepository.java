package com.studyroom.repository;

import com.studyroom.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, Long> {

    List<Campus> findByStatusOrderByIdAsc(Integer status);

    Optional<Campus> findByName(String name);

    boolean existsByName(String name);
}
