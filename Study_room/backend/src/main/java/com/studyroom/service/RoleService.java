package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.dto.PermissionTreeVO;
import com.studyroom.dto.RoleRequest;
import com.studyroom.dto.RoleVO;
import com.studyroom.entity.SysPermission;
import com.studyroom.entity.SysRole;
import com.studyroom.repository.SysPermissionRepository;
import com.studyroom.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    public List<RoleVO> list() {
        return roleRepository.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    public RoleVO get(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return toVO(role);
    }

    @Transactional
    public RoleVO create(RoleRequest request) {
        String code = resolveRoleCode(request.getCode(), request.getName());
        if (roleRepository.findByCode(code).isPresent()) {
            code = "ROLE_" + System.currentTimeMillis();
        }
        SysRole role = new SysRole();
        role.setName(request.getName());
        role.setCode(code);
        role.setDescription(request.getDescription());
        assignPermissions(role, request.getPermissionIds());
        SysRole saved = roleRepository.save(role);
        auditLogService.log("SysRole", saved.getId(), "CREATE", "新建角色: " + saved.getName());
        return toVO(saved);
    }

    @Transactional
    public RoleVO update(Long id, RoleRequest request) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        assignPermissions(role, request.getPermissionIds());
        SysRole saved = roleRepository.save(role);
        auditLogService.log("SysRole", saved.getId(), "UPDATE", "更新角色: " + saved.getName());
        return toVO(saved);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if ("SUPER_ADMIN".equals(role.getCode())) {
            throw new BusinessException("不能删除超级管理员角色");
        }
        roleRepository.delete(role);
        auditLogService.log("SysRole", id, "DELETE", "删除角色: " + role.getName());
    }

    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> all = permissionRepository.findAllByOrderBySortOrderAsc();
        Map<Long, PermissionTreeVO> map = new LinkedHashMap<>();
        for (SysPermission p : all) {
            PermissionTreeVO vo = new PermissionTreeVO();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setName(p.getName() + (p.getType() == 2 ? "（按钮）" : ""));
            vo.setCode(p.getCode());
            vo.setType(p.getType());
            map.put(p.getId(), vo);
        }
        List<PermissionTreeVO> roots = new ArrayList<>();
        for (SysPermission p : all) {
            PermissionTreeVO vo = map.get(p.getId());
            Long parentId = p.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(vo);
            } else {
                PermissionTreeVO parent = map.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    private void assignPermissions(SysRole role, Set<Long> permissionIds) {
        Set<SysPermission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException("存在无效的权限ID");
        }
        role.setPermissions(permissions);
    }

    private RoleVO toVO(SysRole role) {
        Set<Long> permissionIds = role.getPermissions().stream()
                .map(SysPermission::getId).collect(Collectors.toSet());
        List<String> permissionNames = role.getPermissions().stream()
                .map(SysPermission::getName).sorted().collect(Collectors.toList());
        return RoleVO.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .permissionIds(permissionIds)
                .permissionNames(permissionNames)
                .build();
    }

    private String resolveRoleCode(String code, String name) {
        if (code != null && !code.isBlank()) {
            return code.trim();
        }
        String slug = name == null ? "" : name.trim().replaceAll("[^a-zA-Z0-9]+", "_").toUpperCase();
        if (slug.isBlank() || slug.equals("_")) {
            return "ROLE_" + System.currentTimeMillis();
        }
        return "ROLE_" + slug;
    }
}
