package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.dto.*;
import com.studyroom.entity.*;
import com.studyroom.repository.*;
import com.studyroom.security.JwtTokenProvider;
import com.studyroom.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final CampusRepository campusRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    @Value("${app.login.max-fail-count:10}")
    private int maxFailCount;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getEnabled() == 0) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        if (user.getLocked() == 1) {
            throw new BusinessException("账号已锁定，请联系管理员重置密码");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setLoginFailCount(user.getLoginFailCount() + 1);
            if (user.getLoginFailCount() >= maxFailCount) {
                user.setLocked(1);
            }
            userRepository.save(user);
            throw new BusinessException("用户名或密码错误，还可尝试 "
                    + Math.max(0, maxFailCount - user.getLoginFailCount()) + " 次");
        }

        user.setLoginFailCount(0);
        userRepository.save(user);

        if (user.getEmployeeId() != null) {
            // 离职员工不能登录 - checked via enabled flag when employee resigns
        }

        List<String> permissions = collectPermissions(user);
        List<Long> campusIds = user.getCampuses().stream().map(Campus::getId).collect(Collectors.toList());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), campusIds, permissions);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .campusIds(campusIds)
                .permissions(permissions)
                .menus(buildMenus(user))
                .build();
    }

    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        PasswordValidator.validate(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLocked(0);
        user.setLoginFailCount(0);
        userRepository.save(user);
        auditLogService.log("SysUser", userId, "UPDATE", "管理员重置密码");
    }

    @Transactional
    public void updateUserPermission(UserPermissionRequest request) {
        SysUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(request.getEnabled());
        Set<Campus> campuses = new HashSet<>(campusRepository.findAllById(request.getCampusIds()));
        if (campuses.size() != request.getCampusIds().size()) {
            throw new BusinessException("存在无效的校区");
        }
        for (Campus campus : campuses) {
            if (campus.getStatus() != 1) {
                throw new BusinessException("不能分配已停用的校区: " + campus.getName());
            }
        }
        user.setCampuses(campuses);
        Set<SysRole> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
        user.setRoles(roles);
        userRepository.save(user);
        auditLogService.log("SysUser", user.getId(), "UPDATE", "更新用户权限");
    }

    public List<SysPermission> listAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<SysRole> listRoles() {
        return roleRepository.findAll();
    }

    public List<UserSummaryVO> searchUsersByRealName(String realName) {
        if (realName == null || realName.isBlank()) {
            return List.of();
        }
        return userRepository.findByRealNameContaining(realName.trim(), PageRequest.of(0, 20)).stream()
                .map(user -> UserSummaryVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .build())
                .toList();
    }

    public UserPermissionVO getUserPermission(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return UserPermissionVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .campusIds(user.getCampuses().stream().map(Campus::getId).collect(Collectors.toSet()))
                .roleIds(user.getRoles().stream().map(SysRole::getId).collect(Collectors.toSet()))
                .build();
    }

    private List<String> collectPermissions(SysUser user) {
        return user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(SysPermission::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<MenuVO> buildMenus(SysUser user) {
        List<SysPermission> menuPerms = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .filter(p -> p.getType() == 1)
                .collect(Collectors.toMap(SysPermission::getId, p -> p, (a, b) -> a))
                .values().stream()
                .sorted(Comparator.comparing(SysPermission::getSortOrder))
                .toList();

        Map<Long, MenuVO> voMap = new LinkedHashMap<>();
        for (SysPermission p : menuPerms) {
            MenuVO vo = new MenuVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setCode(p.getCode());
            vo.setPath(p.getPath());
            vo.setIcon(p.getIcon());
            voMap.put(p.getId(), vo);
        }

        List<MenuVO> roots = new ArrayList<>();
        for (SysPermission p : menuPerms) {
            MenuVO vo = voMap.get(p.getId());
            Long parentId = p.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(vo);
            } else {
                MenuVO parent = voMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }
}
