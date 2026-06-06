package com.wechat.hosting.repository;
import com.wechat.hosting.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByExternalUserid(String externalUserid);
}
