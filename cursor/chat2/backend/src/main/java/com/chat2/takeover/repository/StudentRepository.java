package com.chat2.takeover.repository;

import com.chat2.takeover.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByIdIn(Collection<Long> ids);
}
