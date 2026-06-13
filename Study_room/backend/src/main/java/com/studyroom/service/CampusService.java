package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.dto.CampusRequest;
import com.studyroom.entity.Campus;
import com.studyroom.entity.SysUser;
import com.studyroom.repository.CampusRepository;
import com.studyroom.repository.SysUserRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepository campusRepository;
    private final SysUserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<Campus> listAll() {
        return campusRepository.findAll(Sort.by("id"));
    }

    public List<Campus> listAuthorizedCampuses() {
        return loadCurrentUserCampuses().stream()
                .sorted(Comparator.comparing(Campus::getId))
                .collect(Collectors.toList());
    }

    public List<Campus> listEnabledCampusesForSelect() {
        return loadCurrentUserCampuses().stream()
                .filter(c -> c.getStatus() == 1)
                .sorted(Comparator.comparing(Campus::getId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Campus create(CampusRequest request) {
        String name = request.getName().trim();
        if (campusRepository.existsByName(name)) {
            throw new BusinessException("校区名称已存在");
        }
        Campus campus = new Campus();
        campus.setName(name);
        campus.setStatus(1);
        Campus saved = campusRepository.save(campus);
        assignCampusToSuperAdmins(saved);
        auditLogService.log("Campus", saved.getId(), "CREATE", "新建校区: " + saved.getName());
        return saved;
    }

    @Transactional
    public Campus updateName(Long id, CampusRequest request) {
        Campus campus = getCampus(id);
        String name = request.getName().trim();
        campusRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("校区名称已存在");
            }
        });
        campus.setName(name);
        Campus saved = campusRepository.save(campus);
        auditLogService.log("Campus", saved.getId(), "UPDATE", "修改校区名称: " + saved.getName());
        return saved;
    }

    @Transactional
    public Campus updateStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("无效的状态值");
        }
        Campus campus = getCampus(id);
        campus.setStatus(status);
        Campus saved = campusRepository.save(campus);
        auditLogService.log("Campus", saved.getId(), "UPDATE",
                (status == 1 ? "启用" : "停用") + "校区: " + saved.getName());
        return saved;
    }

    public void ensureCampusEnabled(Long campusId) {
        if (campusId == null) {
            throw new BusinessException("校区不能为空");
        }
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new BusinessException("校区不存在"));
        if (campus.getStatus() != 1) {
            throw new BusinessException("校区已停用，无法选择");
        }
        SecurityUtils.checkCampusAccess(campusId);
    }

    private Campus getCampus(Long id) {
        return campusRepository.findById(id)
                .orElseThrow(() -> new BusinessException("校区不存在"));
    }

    private List<Campus> loadCurrentUserCampuses() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("登录信息无效，请重新登录");
        }
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return user.getCampuses().stream().collect(Collectors.toList());
    }

    private void assignCampusToSuperAdmins(Campus campus) {
        userRepository.findAll().forEach(user -> {
            boolean isSuperAdmin = user.getRoles().stream()
                    .anyMatch(role -> "SUPER_ADMIN".equals(role.getCode()));
            if (isSuperAdmin) {
                user.getCampuses().add(campus);
                userRepository.save(user);
            }
        });
    }
}
