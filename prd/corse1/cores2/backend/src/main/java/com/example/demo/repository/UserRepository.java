package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @Query(value = "SELECT * FROM students WHERE phone = :phone", nativeQuery = true)
    Optional<User> findByPhone(@Param("phone") String phone);
}
