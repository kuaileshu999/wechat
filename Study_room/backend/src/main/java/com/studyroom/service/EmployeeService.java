package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.PageResult;
import com.studyroom.dto.EmployeeUpdateRequest;
import com.studyroom.entity.Employee;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.repository.EmployeeRepository;
import com.studyroom.repository.SysUserRepository;
import com.studyroom.security.SecurityUtils;
import com.studyroom.util.PhoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SysUserRepository userRepository;
    private final CampusService campusService;
    private final AuditLogService auditLogService;

    public PageResult<Employee> list(String name, int page, int size) {
        Page<Employee> result = name != null && !name.isBlank()
                ? employeeRepository.findByNameContaining(name, PageRequest.of(page - 1, size))
                : employeeRepository.findAll(PageRequest.of(page - 1, size));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<Employee> listActive(Long campusId) {
        if (campusId == null) {
            return employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        }
        return employeeRepository.findByCampusIdAndEmploymentStatus(campusId, EmploymentStatus.ACTIVE);
    }

    public List<Employee> listActiveWithoutUser() {
        return employeeRepository.findActiveWithoutUser(EmploymentStatus.ACTIVE);
    }

    @Transactional
    public Employee create(Employee employee) {
        if (employee.getCampusId() == null) {
            throw new BusinessException("请选择校区");
        }
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new BusinessException("员工姓名不能为空");
        }
        String phone = normalizePhone(employee.getPhone());
        ensurePhoneUnique(phone, null);
        campusService.ensureCampusEnabled(employee.getCampusId());
        employee.setName(employee.getName().trim());
        employee.setPhone(phone);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        Employee saved = employeeRepository.save(employee);
        auditLogService.log("Employee", saved.getId(), "CREATE", "新建员工: " + saved.getName());
        return saved;
    }

    @Transactional
    public Employee update(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        SecurityUtils.checkCampusAccess(employee.getCampusId());
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("员工姓名不能为空");
        }
        String phone = normalizePhone(request.getPhone());
        ensurePhoneUnique(phone, id);
        campusService.ensureCampusEnabled(request.getCampusId());
        SecurityUtils.checkCampusAccess(request.getCampusId());
        employee.setName(request.getName().trim());
        employee.setPhone(phone);
        employee.setCampusId(request.getCampusId());
        Employee saved = employeeRepository.save(employee);
        auditLogService.log("Employee", saved.getId(), "UPDATE", "编辑员工: " + saved.getName());
        return saved;
    }

    @Transactional
    public Employee updateStatus(Long id, EmploymentStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        employee.setEmploymentStatus(status);
        Employee saved = employeeRepository.save(employee);
        if (status == EmploymentStatus.RESIGNED) {
            userRepository.findAll().stream()
                    .filter(u -> id.equals(u.getEmployeeId()))
                    .forEach(u -> {
                        u.setEnabled(0);
                        userRepository.save(u);
                    });
        }
        auditLogService.log("Employee", saved.getId(), "UPDATE", "更新任职状态: " + status);
        return saved;
    }

    private String normalizePhone(String phone) {
        PhoneValidator.validateEmployee(phone);
        return phone.trim();
    }

    private void ensurePhoneUnique(String phone, Long excludeId) {
        boolean exists = excludeId == null
                ? employeeRepository.existsByPhone(phone)
                : employeeRepository.existsByPhoneAndIdNot(phone, excludeId);
        if (exists) {
            throw new BusinessException("手机号已存在");
        }
    }
}
