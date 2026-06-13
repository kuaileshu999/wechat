package com.studyroom.entity;

import com.studyroom.enums.ConsumptionMode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "consumption_record")
public class ConsumptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "campus_id", nullable = false)
    private Long campusId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumption_mode", nullable = false, length = 20)
    private ConsumptionMode consumptionMode;

    @Column(name = "consumed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumedAmount = BigDecimal.ZERO;

    @Column(name = "consumed_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal consumedHours = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "COMPLETED";

    @Column(name = "batch_no", length = 32)
    private String batchNo;

    @Column(length = 500)
    private String remark;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
