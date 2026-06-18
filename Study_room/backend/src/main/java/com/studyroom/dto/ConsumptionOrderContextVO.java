package com.studyroom.dto;

import com.studyroom.enums.ConsumptionMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ConsumptionOrderContextVO {
    private Long orderId;
    private String orderNo;
    private Long campusId;
    private Long teacherId;
    private String teacherName;
    private String teacherNames;
    private String courseName;
    private List<SubjectOptionVO> subjects;
    private Long defaultSubjectId;
    private ConsumptionMode consumptionMode;
    private BigDecimal unitAmount;
    private BigDecimal unitHours;
    private Integer sessionMinutes;
    private BigDecimal paidAmount;
    private Integer totalHours;
    private BigDecimal consumedAmount;
    private BigDecimal pendingAmount;
    private BigDecimal pendingHours;
    private List<ConsumptionRecordVO> completedRecords;
}
