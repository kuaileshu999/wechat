package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.entity.CourseType;
import com.studyroom.repository.CourseTypeRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseTypeService {

    private final CourseTypeRepository courseTypeRepository;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<CourseType> list(int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        Page<CourseType> result = courseTypeRepository.findByCampusIdIn(campusIds, PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<CourseType> listEnabledByCampus(Long campusId) {
        campusService.ensureCampusEnabled(campusId);
        return courseTypeRepository.findByCampusIdAndStatus(campusId, 1);
    }

    @Transactional
    public CourseType create(CourseType courseType) {
        campusService.ensureCampusEnabled(courseType.getCampusId());
        CourseType saved = courseTypeRepository.save(courseType);
        auditLogService.log("CourseType", saved.getId(), "CREATE", "新建课程类型: " + saved.getName());
        return saved;
    }

    @Transactional
    public CourseType updateStatus(Long id, Integer status) {
        CourseType courseType = courseTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程类型不存在"));
        SecurityUtils.checkCampusAccess(courseType.getCampusId());
        courseType.setStatus(status);
        CourseType saved = courseTypeRepository.save(courseType);
        auditLogService.log("CourseType", saved.getId(), "UPDATE", "更新状态: " + status);
        return saved;
    }
}
