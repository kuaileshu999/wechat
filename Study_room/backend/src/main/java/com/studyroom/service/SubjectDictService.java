package com.studyroom.service;

import com.studyroom.common.BusinessException;
import com.studyroom.common.PageResult;
import com.studyroom.dto.SubjectUpdateRequest;
import com.studyroom.entity.SubjectDict;
import com.studyroom.repository.SubjectDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectDictService {

    private final SubjectDictRepository subjectDictRepository;
    private final AuditLogService auditLogService;

    public PageResult<SubjectDict> list(int page, int size) {
        Page<SubjectDict> result = subjectDictRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("name")));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<SubjectDict> listEnabled() {
        return subjectDictRepository.findByStatus(1);
    }

    @Transactional
    public SubjectDict create(SubjectDict subject) {
        String name = subject.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("学科名称不能为空");
        }
        subjectDictRepository.findByName(name).ifPresent(existing -> {
            throw new BusinessException("已存在同名学科");
        });
        subject.setName(name);
        SubjectDict saved = subjectDictRepository.save(subject);
        auditLogService.log("SubjectDict", saved.getId(), "CREATE", "新建学科: " + saved.getName());
        return saved;
    }

    @Transactional
    public SubjectDict update(Long id, SubjectUpdateRequest request) {
        SubjectDict subject = subjectDictRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学科不存在"));
        String name = request.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("学科名称不能为空");
        }
        subjectDictRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("已存在同名学科");
                });
        subject.setName(name);
        SubjectDict saved = subjectDictRepository.save(subject);
        auditLogService.log("SubjectDict", saved.getId(), "UPDATE", "编辑学科: " + saved.getName());
        return saved;
    }

    @Transactional
    public SubjectDict updateStatus(Long id, Integer status) {
        SubjectDict subject = subjectDictRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学科不存在"));
        subject.setStatus(status);
        SubjectDict saved = subjectDictRepository.save(subject);
        auditLogService.log("SubjectDict", saved.getId(), "UPDATE", "更新状态: " + status);
        return saved;
    }

    public SubjectDict ensureEnabled(Long subjectId) {
        SubjectDict subject = subjectDictRepository.findById(subjectId)
                .orElseThrow(() -> new BusinessException("学科不存在"));
        if (subject.getStatus() != 1) {
            throw new BusinessException("学科已停用");
        }
        return subject;
    }
}
