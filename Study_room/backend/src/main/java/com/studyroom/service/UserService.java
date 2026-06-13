package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.PageResult;
import com.studyroom.dto.UserCreateRequest;
import com.studyroom.dto.UserUpdateRequest;
import com.studyroom.dto.UserVO;
import com.studyroom.entity.*;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.repository.*;
import com.studyroom.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final CampusRepository campusRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public PageResult<UserVO> list(String keyword, int page, int size) {
        Page<SysUser> result;
        if (keyword != null && !keyword.isBlank()) {
            result = userRepository.findByRealNameContaining(keyword.trim(), PageRequest.of(page - 1, size));
        } else {
            result = userRepository.findAll(PageRequest.of(page - 1, size));
        }
        List<UserVO> list = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, result.getTotalElements(), page, size);
    }

    public UserVO get(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toVO(user);
    }

    @Transactional
    public UserVO create(UserCreateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("员工不存在"));
        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessException("只能为在职员工创建账号");
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BusinessException("该员工已有系统账号");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        PasswordValidator.validate(request.getPassword());

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(employee.getName());
        user.setPhone(employee.getPhone());
        user.setEmployeeId(employee.getId());
        user.setEnabled(1);
        assignCampuses(user, request.getCampusIds());
        assignRoles(user, request.getRoleIds());
        SysUser saved = userRepository.save(user);
        auditLogService.log("SysUser", saved.getId(), "CREATE", "新建用户: " + saved.getUsername());
        return toVO(saved);
    }

    @Transactional
    public UserVO update(Long id, UserUpdateRequest request) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(request.getEnabled());
        assignCampuses(user, request.getCampusIds());
        assignRoles(user, request.getRoleIds());
        SysUser saved = userRepository.save(user);
        auditLogService.log("SysUser", saved.getId(), "UPDATE", "更新用户权限: " + saved.getUsername());
        return toVO(saved);
    }

    private void assignCampuses(SysUser user, Set<Long> campusIds) {
        Set<Campus> campuses = new HashSet<>(campusRepository.findAllById(campusIds));
        if (campuses.size() != campusIds.size()) {
            throw new BusinessException("存在无效的校区");
        }
        for (Campus campus : campuses) {
            if (campus.getStatus() != 1) {
                throw new BusinessException("不能分配已停用的校区: " + campus.getName());
            }
        }
        user.setCampuses(campuses);
    }

    private void assignRoles(SysUser user, Set<Long> roleIds) {
        Set<SysRole> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        if (roles.size() != roleIds.size()) {
            throw new BusinessException("存在无效的角色");
        }
        user.setRoles(roles);
    }

    private UserVO toVO(SysUser user) {
        String employeeName = null;
        if (user.getEmployeeId() != null) {
            employeeName = employeeRepository.findById(user.getEmployeeId())
                    .map(Employee::getName).orElse(null);
        }
        List<String> campusNames = user.getCampuses().stream()
                .map(Campus::getName).sorted().collect(Collectors.toList());
        List<String> roleNames = user.getRoles().stream()
                .map(SysRole::getName).sorted().collect(Collectors.toList());
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .employeeId(user.getEmployeeId())
                .employeeName(employeeName)
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .campusIds(user.getCampuses().stream().map(Campus::getId).collect(Collectors.toSet()))
                .campusNames(campusNames)
                .roleIds(user.getRoles().stream().map(SysRole::getId).collect(Collectors.toSet()))
                .roleNames(roleNames)
                .build();
    }
}
