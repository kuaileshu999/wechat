package com.studyroom.dto;

import com.studyroom.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleVO {
    private Long id;
    private Long campusId;
    private String campusName;
    private Long teacherId;
    private String teacherName;
    private Long courseTypeId;
    private String courseTypeName;
    private Long studentId;
    private String studentName;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String classroom;
    private String remark;
    private ScheduleStatus status;
    private Boolean editable;
    private Boolean deletable;
}
