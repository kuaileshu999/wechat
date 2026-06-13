package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.ScheduleBatchCreateRequest;
import com.studyroom.dto.ScheduleBatchDeleteRequest;
import com.studyroom.dto.ScheduleRequest;
import com.studyroom.dto.ScheduleVO;
import com.studyroom.entity.*;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.enums.ScheduleStatus;
import com.studyroom.repository.*;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final LessonScheduleRepository scheduleRepository;
    private final CampusRepository campusRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final CourseTypeRepository courseTypeRepository;
    private final SysUserRepository userRepository;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<ScheduleVO> list(Long campusId, Long teacherId, Long courseTypeId,
                                         ScheduleStatus status, LocalDate startDate, LocalDate endDate,
                                         int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
        }
        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        Page<LessonSchedule> result = scheduleRepository.search(
                CampusScope.currentCampusIds(), campusId, teacherId, courseTypeId, status,
                startTime, endTime, PageRequest.of(page - 1, size));
        List<ScheduleVO> list = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, result.getTotalElements(), page, size);
    }

    public List<ScheduleVO> listMy(LocalDate startDate, LocalDate endDate, Long courseTypeId) {
        Long employeeId = getCurrentEmployeeId();
        if (employeeId == null) {
            return List.of();
        }
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDate.now().plusDays(30).atTime(LocalTime.MAX);
        return scheduleRepository.findByTeacherIdAndStartTimeBetweenOrderByStartTimeAsc(employeeId, start, end)
                .stream()
                .filter(s -> courseTypeId == null || courseTypeId.equals(s.getCourseTypeId()))
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleVO create(ScheduleRequest request) {
        LessonSchedule saved = createInternal(request);
        auditLogService.log("LessonSchedule", saved.getId(), "CREATE", "新建排课: " + formatSummary(saved));
        return toVO(saved);
    }

    @Transactional
    public int batchCreate(ScheduleBatchCreateRequest request) {
        List<LessonSchedule> saved = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            try {
                saved.add(createInternal(request.getItems().get(i)));
            } catch (BusinessException e) {
                throw new BusinessException("第 " + (i + 1) + " 条: " + e.getMessage());
            }
        }
        auditLogService.log("LessonSchedule", 0L, "CREATE", "批量排课 " + saved.size() + " 条");
        return saved.size();
    }

    @Transactional
    public int importSchedules(MultipartFile file, Long campusId) throws Exception {
        campusService.ensureCampusEnabled(campusId);
        List<ScheduleRequest> requests = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                try {
                    requests.add(parseImportRow(row, campusId, i + 1));
                } catch (BusinessException e) {
                    throw new BusinessException("第 " + (i + 1) + " 行: " + e.getMessage());
                }
            }
        }
        if (requests.isEmpty()) {
            throw new BusinessException("导入文件无有效数据");
        }
        List<LessonSchedule> saved = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            try {
                saved.add(createInternal(requests.get(i)));
            } catch (BusinessException e) {
                throw new BusinessException("第 " + (i + 2) + " 行: " + e.getMessage());
            }
        }
        auditLogService.log("LessonSchedule", 0L, "CREATE", "导入排课 " + saved.size() + " 条");
        return saved.size();
    }

    private LessonSchedule createInternal(ScheduleRequest request) {
        validateRequest(request);
        LessonSchedule schedule = buildSchedule(new LessonSchedule(), request);
        schedule.setStatus(ScheduleStatus.PENDING);
        schedule.setCreatedBy(SecurityUtils.getCurrentUserId());
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public ScheduleVO update(Long id, ScheduleRequest request) {
        LessonSchedule schedule = getSchedule(id);
        ensurePending(schedule, "只能修改未上的排课");
        validateRequest(request);
        buildSchedule(schedule, request);
        LessonSchedule saved = scheduleRepository.save(schedule);
        auditLogService.log("LessonSchedule", saved.getId(), "UPDATE", "修改排课: " + formatSummary(saved));
        return toVO(saved);
    }

    @Transactional
    public void delete(Long id) {
        LessonSchedule schedule = getSchedule(id);
        ensurePending(schedule, "只能删除未上的排课");
        scheduleRepository.delete(schedule);
        auditLogService.log("LessonSchedule", id, "DELETE", "删除排课: " + formatSummary(schedule));
    }

    @Transactional
    public int batchDelete(ScheduleBatchDeleteRequest request) {
        List<LessonSchedule> schedules = scheduleRepository.findByIdIn(request.getIds());
        if (schedules.isEmpty()) {
            throw new BusinessException("未找到排课记录");
        }
        int deleted = 0;
        for (LessonSchedule schedule : schedules) {
            SecurityUtils.checkCampusAccess(schedule.getCampusId());
            if (schedule.getStatus() != ScheduleStatus.PENDING) {
                continue;
            }
            scheduleRepository.delete(schedule);
            auditLogService.log("LessonSchedule", schedule.getId(), "DELETE", "批量删除排课: " + formatSummary(schedule));
            deleted++;
        }
        if (deleted == 0) {
            throw new BusinessException("所选排课均已上课，无法删除");
        }
        return deleted;
    }

    @Transactional
    public ScheduleVO complete(Long id) {
        LessonSchedule schedule = getSchedule(id);
        SecurityUtils.checkCampusAccess(schedule.getCampusId());
        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new BusinessException("该排课已标记为已上课");
        }
        schedule.setStatus(ScheduleStatus.COMPLETED);
        LessonSchedule saved = scheduleRepository.save(schedule);
        auditLogService.log("LessonSchedule", saved.getId(), "UPDATE", "标记已上课: " + formatSummary(saved));
        return toVO(saved);
    }

    private LessonSchedule getSchedule(Long id) {
        LessonSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排课不存在"));
        SecurityUtils.checkCampusAccess(schedule.getCampusId());
        return schedule;
    }

    private void ensurePending(LessonSchedule schedule, String message) {
        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new BusinessException(message);
        }
    }

    private void validateRequest(ScheduleRequest request) {
        request.setStartTime(normalizeScheduleTime(request.getStartTime(), "开始时间"));
        request.setEndTime(normalizeScheduleTime(request.getEndTime(), "结束时间"));
        campusService.ensureCampusEnabled(request.getCampusId());
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException("授课老师不存在"));
        if (teacher.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessException("只能为在职老师排课");
        }
        if (!teacher.getCampusId().equals(request.getCampusId())) {
            throw new BusinessException("老师不属于该校区");
        }
        ensureEnabledCourseType(request.getCampusId(), request.getCourseTypeId());
        if (request.getStudentId() != null) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new BusinessException("学员不存在"));
            if (!student.getCampusId().equals(request.getCampusId())) {
                throw new BusinessException("学员不属于该校区");
            }
        }
    }

    private LessonSchedule buildSchedule(LessonSchedule schedule, ScheduleRequest request) {
        schedule.setCampusId(request.getCampusId());
        schedule.setTeacherId(request.getTeacherId());
        schedule.setCourseTypeId(request.getCourseTypeId());
        schedule.setStudentId(request.getStudentId());
        schedule.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setClassroom(request.getClassroom() != null ? request.getClassroom().trim() : null);
        schedule.setRemark(request.getRemark());
        return schedule;
    }

    private ScheduleVO toVO(LessonSchedule schedule) {
        String campusName = campusRepository.findById(schedule.getCampusId()).map(Campus::getName).orElse("");
        String teacherName = employeeRepository.findById(schedule.getTeacherId()).map(Employee::getName).orElse("");
        String studentName = schedule.getStudentId() == null ? null
                : studentRepository.findById(schedule.getStudentId()).map(Student::getName).orElse("");
        String courseTypeName = courseTypeRepository.findById(schedule.getCourseTypeId())
                .map(CourseType::getName).orElse("");
        boolean pending = schedule.getStatus() == ScheduleStatus.PENDING;
        return ScheduleVO.builder()
                .id(schedule.getId())
                .campusId(schedule.getCampusId())
                .campusName(campusName)
                .teacherId(schedule.getTeacherId())
                .teacherName(teacherName)
                .courseTypeId(schedule.getCourseTypeId())
                .courseTypeName(courseTypeName)
                .studentId(schedule.getStudentId())
                .studentName(studentName)
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .classroom(schedule.getClassroom())
                .remark(schedule.getRemark())
                .status(schedule.getStatus())
                .editable(pending)
                .deletable(pending)
                .build();
    }

    private Long getCurrentEmployeeId() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .map(SysUser::getEmployeeId)
                .orElse(null);
    }

    private String formatSummary(LessonSchedule schedule) {
        String teacher = employeeRepository.findById(schedule.getTeacherId()).map(Employee::getName).orElse("");
        return teacher + " " + schedule.getStartTime();
    }

    private ScheduleRequest parseImportRow(Row row, Long campusId, int rowNum) {
        String teacherName = getCellString(row.getCell(0));
        String typeText = getCellString(row.getCell(1));
        String studentText = getCellString(row.getCell(2));
        String title = getCellString(row.getCell(3));
        LocalDateTime startTime = normalizeScheduleTime(getCellDateTime(row.getCell(4)), "开始时间");
        LocalDateTime endTime = normalizeScheduleTime(getCellDateTime(row.getCell(5)), "结束时间");
        String remark = getCellString(row.getCell(6));

        if (teacherName == null || teacherName.isBlank()) {
            throw new BusinessException("授课老师不能为空");
        }
        if (typeText == null || typeText.isBlank()) {
            throw new BusinessException("课程类型不能为空");
        }
        if (startTime == null || endTime == null) {
            throw new BusinessException("开始/结束时间格式不正确");
        }

        Employee teacher = resolveTeacher(campusId, teacherName.trim());
        CourseType courseType = resolveCourseType(campusId, typeText);
        Long studentId = resolveStudentId(campusId, studentText);

        ScheduleRequest request = new ScheduleRequest();
        request.setCampusId(campusId);
        request.setTeacherId(teacher.getId());
        request.setCourseTypeId(courseType.getId());
        request.setStudentId(studentId);
        request.setTitle(title);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setClassroom(null);
        request.setRemark(remark);
        return request;
    }

    private Employee resolveTeacher(Long campusId, String teacherName) {
        List<Employee> teachers = employeeRepository.findByCampusIdAndNameAndEmploymentStatus(
                campusId, teacherName, EmploymentStatus.ACTIVE);
        if (teachers.isEmpty()) {
            throw new BusinessException("未找到在职老师: " + teacherName);
        }
        if (teachers.size() > 1) {
            throw new BusinessException("存在多名同名老师: " + teacherName);
        }
        return teachers.get(0);
    }

    private Long resolveStudentId(Long campusId, String studentText) {
        if (studentText == null || studentText.isBlank()) {
            return null;
        }
        String text = studentText.trim();
        if (text.matches("\\d{11}")) {
            Student student = studentRepository.findByCampusIdAndPhone(campusId, text)
                    .orElseThrow(() -> new BusinessException("未找到学员手机号: " + text));
            return student.getId();
        }
        List<Student> students = studentRepository.findByCampusIdAndName(campusId, text);
        if (students.isEmpty()) {
            throw new BusinessException("未找到学员: " + text);
        }
        if (students.size() > 1) {
            throw new BusinessException("存在多名同名学员: " + text);
        }
        return students.get(0).getId();
    }

    private CourseType ensureEnabledCourseType(Long campusId, Long courseTypeId) {
        CourseType courseType = courseTypeRepository.findById(courseTypeId)
                .orElseThrow(() -> new BusinessException("课程类型不存在"));
        if (!courseType.getCampusId().equals(campusId)) {
            throw new BusinessException("课程类型不属于该校区");
        }
        if (courseType.getStatus() != 1) {
            throw new BusinessException("课程类型已停用");
        }
        return courseType;
    }

    private CourseType resolveCourseType(Long campusId, String text) {
        List<CourseType> types = courseTypeRepository.findByCampusIdAndNameAndStatus(
                campusId, text.trim(), 1);
        if (types.isEmpty()) {
            throw new BusinessException("未找到启用的课程类型: " + text.trim());
        }
        if (types.size() > 1) {
            throw new BusinessException("存在多个同名课程类型: " + text.trim());
        }
        return types.get(0);
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 6; i++) {
            String val = getCellString(row.getCell(i));
            if (val != null && !val.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.getStringCellValue();
    }

    private LocalDateTime getCellDateTime(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return normalizeScheduleTime(cell.getLocalDateTimeCellValue(), "时间");
        }
        String text = getCellString(cell);
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();
        String[] patterns = {"yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                return normalizeScheduleTime(LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern)), "时间");
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private LocalDateTime normalizeScheduleTime(LocalDateTime time, String label) {
        if (time == null) {
            return null;
        }
        LocalDateTime normalized = time.withSecond(0).withNano(0);
        if (normalized.getMinute() % 5 != 0) {
            throw new BusinessException(label + "必须为5分钟的整数倍");
        }
        return normalized;
    }
}
