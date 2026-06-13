package com.studyroom.repository;

import com.studyroom.entity.Employee;
import com.studyroom.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByNameContaining(String name, Pageable pageable);

    List<Employee> findByEmploymentStatus(EmploymentStatus status);

    List<Employee> findByCampusIdAndEmploymentStatus(Long campusId, EmploymentStatus status);

    List<Employee> findByCampusIdAndNameAndEmploymentStatus(Long campusId, String name, EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = :status "
            + "AND NOT EXISTS (SELECT u FROM SysUser u WHERE u.employeeId = e.id)")
    List<Employee> findActiveWithoutUser(@Param("status") EmploymentStatus status);
}
