package com.studyroom.entity;

import com.studyroom.enums.ConsumptionMode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campus_id", nullable = false)
    private Long campusId;

    @Column(name = "course_type_id", nullable = false)
    private Long courseTypeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumption_mode", nullable = false, length = 20)
    private ConsumptionMode consumptionMode;

    @Column(name = "unit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitAmount;

    @Column(name = "unit_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitHours = BigDecimal.ONE;

    @Column(name = "session_minutes", nullable = false)
    private Integer sessionMinutes = 60;

    @Column(nullable = false)
    private Integer status = 1;

    @Transient
    private List<Long> subjectIds;

    @Transient
    private String subjectName;

    @Transient
    private String gradeName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
