package com.studyroom.repository;

import com.studyroom.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Page<Student> findByCampusIdIn(List<Long> campusIds, Pageable pageable);
    Page<Student> findByCampusIdInAndNameContaining(List<Long> campusIds, String name, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.campusId IN :campusIds AND (s.name LIKE %:keyword% OR s.phone LIKE %:keyword%)")
    List<Student> searchByKeyword(@Param("campusIds") List<Long> campusIds, @Param("keyword") String keyword);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<Student> findByPhone(String phone);

    Optional<Student> findByCampusIdAndPhone(Long campusId, String phone);

    List<Student> findByCampusIdAndName(Long campusId, String name);
}
