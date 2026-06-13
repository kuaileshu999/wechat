package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.PageResult;
import com.studyroom.entity.Employee;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.repository.EmployeeRepository;
import com.studyroom.repository.SysUserRepository;
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
        campusService.ensureCampusEnabled(employee.getCampusId());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        Employee saved = employeeRepository.save(employee);
        auditLogService.log("Employee", saved.getId(), "CREATE", "新建员工: " + saved.getName());
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
}
