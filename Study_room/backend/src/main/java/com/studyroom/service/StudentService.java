package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.CampusScope;
import com.studyroom.common.PageResult;
import com.studyroom.dto.StudentUpdateRequest;
import com.studyroom.entity.Student;
import com.studyroom.repository.StudentRepository;
import com.studyroom.util.PhoneValidator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<Student> list(String name, int page, int size) {
        if (CampusScope.isEmpty()) {
            return CampusScope.emptyPage(page, size);
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        Page<Student> result = name != null && !name.isBlank()
                ? studentRepository.findByCampusIdInAndNameContaining(campusIds, name, PageRequest.of(page - 1, size))
                : studentRepository.findByCampusIdIn(campusIds, PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<Student> search(String keyword) {
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return studentRepository.searchByKeyword(campusIds, keyword);
    }

    @Transactional
    public Student create(Student student) {
        validateName(student.getName());
        String phone = normalizePhone(student.getPhone());
        ensurePhoneUnique(phone, null);
        campusService.ensureCampusEnabled(student.getCampusId());
        student.setName(student.getName().trim());
        student.setPhone(phone);
        Student saved = studentRepository.save(student);
        auditLogService.log("Student", saved.getId(), "CREATE", "新建学员: " + saved.getName());
        return saved;
    }

    @Transactional
    public Student update(Long id, StudentUpdateRequest request) {
        Student student = getAuthorizedStudent(id);
        validateName(request.getName());
        String phone = normalizePhone(request.getPhone());
        ensurePhoneUnique(phone, id);
        campusService.ensureCampusEnabled(request.getCampusId());
        if (!CampusScope.currentCampusIds().contains(request.getCampusId())) {
            throw new BusinessException("无权选择该校区");
        }
        student.setName(request.getName().trim());
        student.setPhone(phone);
        student.setCampusId(request.getCampusId());
        Student saved = studentRepository.save(student);
        auditLogService.log("Student", saved.getId(), "UPDATE", "修改学员: " + saved.getName());
        return saved;
    }

    @Transactional
    public int importStudents(MultipartFile file, Long campusId) throws Exception {
        campusService.ensureCampusEnabled(campusId);
        List<Student> students = new ArrayList<>();
        Set<String> phonesInFile = new HashSet<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String name = getCellString(row.getCell(0));
                String phoneRaw = getCellString(row.getCell(1));
                String remark = getCellString(row.getCell(2));
                if (name == null || name.isBlank()) continue;
                validateName(name);
                String phone = normalizePhone(phoneRaw);
                if (!phonesInFile.add(phone)) {
                    throw new BusinessException("导入文件第 " + (i + 1) + " 行手机号重复: " + phone);
                }
                ensurePhoneUnique(phone, null);
                Student student = new Student();
                student.setName(name.trim());
                student.setPhone(phone);
                student.setCampusId(campusId);
                student.setRemark(remark);
                students.add(student);
            }
        }
        if (students.isEmpty()) {
            throw new BusinessException("导入文件无有效数据");
        }
        studentRepository.saveAll(students);
        auditLogService.log("Student", 0L, "CREATE", "批量导入学员 " + students.size() + " 条");
        return students.size();
    }

    private Student getAuthorizedStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学员不存在"));
        if (!CampusScope.currentCampusIds().contains(student.getCampusId())) {
            throw new BusinessException("无权修改该学员");
        }
        return student;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("学员姓名不能为空");
        }
    }

    private String normalizePhone(String phone) {
        PhoneValidator.validate(phone);
        return phone.trim();
    }

    private void ensurePhoneUnique(String phone, Long excludeId) {
        boolean exists = excludeId == null
                ? studentRepository.existsByPhone(phone)
                : studentRepository.existsByPhoneAndIdNot(phone, excludeId);
        if (exists) {
            throw new BusinessException("手机号已存在");
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.getStringCellValue();
    }
}
