package com.studyroom.repository;

import com.studyroom.entity.LessonSchedule;
import com.studyroom.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {

    @Query("""
            SELECT s FROM LessonSchedule s
            WHERE s.campusId IN :campusIds
            AND (:campusId IS NULL OR s.campusId = :campusId)
            AND (:teacherId IS NULL OR s.teacherId = :teacherId)
            AND (:courseTypeId IS NULL OR s.courseTypeId = :courseTypeId)
            AND (:status IS NULL OR s.status = :status)
            AND (:startTime IS NULL OR s.startTime >= :startTime)
            AND (:endTime IS NULL OR s.startTime <= :endTime)
            ORDER BY s.startTime ASC
            """)
    Page<LessonSchedule> search(@Param("campusIds") List<Long> campusIds,
                                @Param("campusId") Long campusId,
                                @Param("teacherId") Long teacherId,
                                @Param("courseTypeId") Long courseTypeId,
                                @Param("status") ScheduleStatus status,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                Pageable pageable);

    List<LessonSchedule> findByTeacherIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long teacherId, LocalDateTime startTime, LocalDateTime endTime);

    List<LessonSchedule> findByIdIn(List<Long> ids);
}
