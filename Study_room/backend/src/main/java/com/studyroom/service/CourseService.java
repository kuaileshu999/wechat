package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.CourseUpdateRequest;
import com.studyroom.entity.Course;
import com.studyroom.entity.CourseSubject;
import com.studyroom.entity.Grade;
import com.studyroom.entity.SubjectDict;
import com.studyroom.enums.ConsumptionMode;
import com.studyroom.repository.CourseRepository;
import com.studyroom.repository.CourseSubjectRepository;
import com.studyroom.repository.GradeRepository;
import com.studyroom.repository.SubjectDictRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final GradeRepository gradeRepository;
    private final SubjectDictRepository subjectDictRepository;
    private final GradeService gradeService;
    private final SubjectDictService subjectDictService;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<Course> list(Long campusId, String name, Long subjectId, Long gradeId, int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
        }
        String nameKeyword = name != null && !name.isBlank() ? name.trim() : null;
        Page<Course> result = courseRepository.search(campusIds, campusId, nameKeyword, subjectId, gradeId,
                PageRequest.of(page - 1, size));
        enrichCourses(result.getContent());
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<Course> listEnabledByCampus(Long campusId) {
        campusService.ensureCampusEnabled(campusId);
        List<Course> courses = courseRepository.findByCampusIdAndStatus(campusId, 1);
        enrichCourses(courses);
        return courses;
    }

    public List<Course> searchEnabled(Long campusId, String keyword) {
        campusService.ensureCampusEnabled(campusId);
        SecurityUtils.checkCampusAccess(campusId);
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        List<Course> courses = courseRepository.searchEnabledByCampusAndName(campusId, keyword.trim());
        enrichCourses(courses);
        return courses;
    }

    public List<Long> getSubjectIds(Long courseId) {
        return courseSubjectRepository.findByCourseId(courseId).stream()
                .map(CourseSubject::getSubjectId)
                .toList();
    }

    public List<SubjectDict> getSubjects(Long courseId) {
        List<Long> subjectIds = getSubjectIds(courseId);
        if (subjectIds.isEmpty()) {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null && course.getSubjectId() != null) {
                subjectIds = List.of(course.getSubjectId());
            }
        }
        return subjectDictRepository.findAllById(subjectIds);
    }

    @Transactional
    public Course create(Course course) {
        campusService.ensureCampusEnabled(course.getCampusId());
        List<Long> subjectIds = normalizeSubjectIds(course.getSubjectIds(), course.getSubjectId());
        validateSubjectIds(subjectIds);
        gradeService.ensureEnabled(course.getGradeId());
        course.setSubjectId(subjectIds.get(0));
        validateConsumptionFields(course);
        Course saved = courseRepository.save(course);
        saveCourseSubjects(saved.getId(), subjectIds);
        enrichCourses(List.of(saved));
        auditLogService.log("Course", saved.getId(), "CREATE", "新建课程: " + saved.getName());
        return saved;
    }

    @Transactional
    public Course update(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        SecurityUtils.checkCampusAccess(course.getCampusId());
        List<Long> subjectIds = normalizeSubjectIds(request.getSubjectIds(), null);
        validateSubjectIds(subjectIds);
        gradeService.ensureEnabled(request.getGradeId());
        course.setName(request.getName().trim());
        course.setSubjectId(subjectIds.get(0));
        course.setGradeId(request.getGradeId());
        course.setConsumptionMode(request.getConsumptionMode());
        course.setUnitAmount(request.getUnitAmount());
        course.setUnitHours(request.getUnitHours());
        course.setSessionMinutes(request.getSessionMinutes());
        validateConsumptionFields(course);
        Course saved = courseRepository.save(course);
        saveCourseSubjects(saved.getId(), subjectIds);
        enrichCourses(List.of(saved));
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

    private List<Long> normalizeSubjectIds(List<Long> subjectIds, Long fallbackSubjectId) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (subjectIds != null) {
            subjectIds.stream().filter(id -> id != null).forEach(ids::add);
        }
        if (ids.isEmpty() && fallbackSubjectId != null) {
            ids.add(fallbackSubjectId);
        }
        if (ids.isEmpty()) {
            throw new BusinessException("请至少选择一个学科");
        }
        return new ArrayList<>(ids);
    }

    private void validateSubjectIds(List<Long> subjectIds) {
        for (Long subjectId : subjectIds) {
            subjectDictService.ensureEnabled(subjectId);
        }
    }

    private void saveCourseSubjects(Long courseId, List<Long> subjectIds) {
        courseSubjectRepository.deleteByCourseId(courseId);
        for (Long subjectId : subjectIds) {
            CourseSubject cs = new CourseSubject();
            cs.setCourseId(courseId);
            cs.setSubjectId(subjectId);
            courseSubjectRepository.save(cs);
        }
    }

    private void enrichCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            return;
        }
        Set<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> subjectIdsByCourse = courseSubjectRepository.findByCourseIdIn(courseIds).stream()
                .collect(Collectors.groupingBy(CourseSubject::getCourseId,
                        Collectors.mapping(CourseSubject::getSubjectId, Collectors.toList())));

        Set<Long> allSubjectIds = new LinkedHashSet<>();
        Set<Long> gradeIds = new LinkedHashSet<>();
        for (Course course : courses) {
            List<Long> subjectIds = subjectIdsByCourse.getOrDefault(course.getId(), List.of());
            if (subjectIds.isEmpty() && course.getSubjectId() != null) {
                subjectIds = List.of(course.getSubjectId());
            }
            course.setSubjectIds(subjectIds);
            allSubjectIds.addAll(subjectIds);
            gradeIds.add(course.getGradeId());
        }

        Map<Long, String> subjectNames = subjectDictRepository.findAllById(allSubjectIds).stream()
                .collect(Collectors.toMap(SubjectDict::getId, SubjectDict::getName));
        Map<Long, String> gradeNames = gradeRepository.findAllById(gradeIds).stream()
                .collect(Collectors.toMap(Grade::getId, Grade::getName));

        for (Course course : courses) {
            List<Long> subjectIds = course.getSubjectIds();
            if (subjectIds == null || subjectIds.isEmpty()) {
                course.setSubjectName(subjectNames.get(course.getSubjectId()));
            } else {
                course.setSubjectName(subjectIds.stream()
                        .map(subjectNames::get)
                        .filter(name -> name != null && !name.isBlank())
                        .collect(Collectors.joining("、")));
            }
            course.setGradeName(gradeNames.get(course.getGradeId()));
        }
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
