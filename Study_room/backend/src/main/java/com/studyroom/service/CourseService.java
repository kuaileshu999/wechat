package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.entity.Course;
import com.studyroom.repository.CourseRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Course saved = courseRepository.save(course);
        auditLogService.log("Course", saved.getId(), "CREATE", "新建课程: " + saved.getName());
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
}
