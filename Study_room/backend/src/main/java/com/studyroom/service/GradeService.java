package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.PageResult;
import com.studyroom.dto.GradeUpdateRequest;
import com.studyroom.entity.Grade;
import com.studyroom.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final AuditLogService auditLogService;

    public PageResult<Grade> list(int page, int size) {
        Page<Grade> result = gradeRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("name")));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<Grade> listEnabled() {
        return gradeRepository.findByStatus(1);
    }

    @Transactional
    public Grade create(Grade grade) {
        String name = grade.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("年级名称不能为空");
        }
        gradeRepository.findByName(name).ifPresent(existing -> {
            throw new BusinessException("已存在同名年级");
        });
        grade.setName(name);
        Grade saved = gradeRepository.save(grade);
        auditLogService.log("Grade", saved.getId(), "CREATE", "新建年级: " + saved.getName());
        return saved;
    }

    @Transactional
    public Grade update(Long id, GradeUpdateRequest request) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年级不存在"));
        String name = request.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("年级名称不能为空");
        }
        gradeRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("已存在同名年级");
                });
        grade.setName(name);
        Grade saved = gradeRepository.save(grade);
        auditLogService.log("Grade", saved.getId(), "UPDATE", "编辑年级: " + saved.getName());
        return saved;
    }

    @Transactional
    public Grade updateStatus(Long id, Integer status) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年级不存在"));
        grade.setStatus(status);
        Grade saved = gradeRepository.save(grade);
        auditLogService.log("Grade", saved.getId(), "UPDATE", "更新状态: " + status);
        return saved;
    }

    public Grade ensureEnabled(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new BusinessException("年级不存在"));
        if (grade.getStatus() != 1) {
            throw new BusinessException("年级已停用");
        }
        return grade;
    }
}
