package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.CourseUpdateRequest;
import com.studyroom.entity.Course;
import com.studyroom.enums.ConsumptionMode;
import com.studyroom.repository.CourseRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<Course> list(int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        Page<Course> result = courseRepository.findByCampusIdIn(campusIds, PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<Course> listEnabledByCampus(Long campusId) {
        campusService.ensureCampusEnabled(campusId);
        return courseRepository.findByCampusIdAndStatus(campusId, 1);
    }

    public List<Course> searchEnabled(Long campusId, String keyword) {
        campusService.ensureCampusEnabled(campusId);
        SecurityUtils.checkCampusAccess(campusId);
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return courseRepository.searchEnabledByCampusAndName(campusId, keyword.trim());
    }

    @Transactional
    public Course create(Course course) {
        campusService.ensureCampusEnabled(course.getCampusId());
        validateConsumptionFields(course);
        Course saved = courseRepository.save(course);
        auditLogService.log("Course", saved.getId(), "CREATE", "新建课程: " + saved.getName());
        return saved;
    }

    @Transactional
    public Course update(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        SecurityUtils.checkCampusAccess(course.getCampusId());
        course.setName(request.getName().trim());
        course.setSubject(request.getSubject());
        course.setConsumptionMode(request.getConsumptionMode());
        course.setUnitAmount(request.getUnitAmount());
        course.setUnitHours(request.getUnitHours());
        course.setSessionMinutes(request.getSessionMinutes());
        validateConsumptionFields(course);
        Course saved = courseRepository.save(course);
        auditLogService.log("Course", saved.getId(), "UPDATE", "修改课程: " + saved.getName());
        return saved;
    }

    @Transactional
    public Course updateStatus(Long id, Integer status) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        SecurityUtils.checkCampusAccess(course.getCampusId());
        course.setStatus(status);
        Course saved = courseRepository.save(course);
        auditLogService.log("Course", saved.getId(), "UPDATE", "更新状态: " + status);
        return saved;
    }

    private void validateConsumptionFields(Course course) {
        if (course.getConsumptionMode() == null) {
            course.setConsumptionMode(ConsumptionMode.HOURS);
        }
        if (course.getSessionMinutes() == null || course.getSessionMinutes() < 1) {
            throw new BusinessException("每次消课时长必须为正整数");
        }
        if (course.getConsumptionMode() == ConsumptionMode.HOURS) {
            if (course.getUnitHours() == null || course.getUnitHours().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("每次消课课时必须大于0");
            }
            if (course.getUnitAmount() == null) {
                course.setUnitAmount(BigDecimal.ZERO);
            }
        } else if (course.getConsumptionMode() == ConsumptionMode.AMOUNT) {
            if (course.getUnitAmount() == null || course.getUnitAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("每次消课金额必须大于0");
            }
            if (course.getUnitHours() == null) {
                course.setUnitHours(BigDecimal.ZERO);
            }
        }
    }
}
