package com.studyroom.dto;

import com.studyroom.enums.ConsumptionMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ConsumptionRecordVO {
    private Long id;
    private Long orderId;
    private Long campusId;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private String teacherName;
    private String studentName;
    private String studentPhone;
    private ConsumptionMode consumptionMode;
    private BigDecimal consumedAmount;
    private BigDecimal consumedHours;
    private String status;
    private String batchNo;
    private String remark;
    private LocalDateTime classTime;
    private LocalDateTime classEndTime;
    private Integer sessionMinutes;
    private LocalDateTime createdAt;
}
