package com.chat2.takeover.service;

import com.chat2.takeover.dto.*;
import com.chat2.takeover.entity.TakeoverAssignment;
import com.chat2.takeover.entity.Tutor;
import com.chat2.takeover.entity.WecomAccount;
import com.chat2.takeover.repository.TakeoverAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TakeoverAssignmentService {

    private final TakeoverAssignmentRepository assignmentRepository;
    private final TutorService tutorService;
    private final WecomAccountService wecomAccountService;
    private final OrgService orgService;

    public TakeoverAssignmentService(
            TakeoverAssignmentRepository assignmentRepository,
            TutorService tutorService,
            WecomAccountService wecomAccountService,
            OrgService orgService) {
        this.assignmentRepository = assignmentRepository;
        this.tutorService = tutorService;
        this.wecomAccountService = wecomAccountService;
        this.orgService = orgService;
    }

    public List<TakeoverAssignmentVO> listAll() {
        Map<Long, String> tutorNames = tutorService.listAll().stream()
                .collect(Collectors.toMap(TutorVO::id, TutorVO::name));
        Map<Long, WecomAccount> accountMap = wecomAccountService.requireAllMap();
        Map<Long, String> tutorOrgPaths = buildTutorOrgPathMap();

        return assignmentRepository.findAllByOrderByIdDesc().stream()
                .map(a -> toVO(a, tutorNames, accountMap, tutorOrgPaths))
                .toList();
    }

    @Transactional
    public TakeoverAssignmentVO save(TakeoverAssignmentRequest request) {
        TakeoverAssignment saved = saveOne(
                request.sourceTutorId(),
                request.wecomAccountId(),
                request.takeoverTutorId(),
                request.enabled());
        Map<Long, String> tutorNames = Map.of(
                saved.getSourceTutorId(), tutorService.require(saved.getSourceTutorId()).getName(),
                saved.getTakeoverTutorId(), tutorService.require(saved.getTakeoverTutorId()).getName());
        return toVO(saved, tutorNames, wecomAccountService.requireAllMap(), buildTutorOrgPathMap());
    }

    @Transactional
    public BatchTakeoverResultVO batchSave(BatchTakeoverRequest request) {
        tutorService.require(request.takeoverTutorId());
        Set<Long> tutorIds = new LinkedHashSet<>(request.sourceTutorIds());
        for (Long tutorId : tutorIds) {
            Tutor t = tutorService.require(tutorId);
            if (Boolean.TRUE.equals(t.getTakeoverRole())) {
                throw new IllegalArgumentException("不能接管接管辅导角色: " + t.getName());
            }
        }

        int created = 0;
        int updated = 0;
        List<Long> accountIds = request.wecomAccountIds();

        if (accountIds.isEmpty() || request.assignAllAccounts()) {
            for (Long tutorId : tutorIds) {
                boolean isNew = saveOneIfNeeded(tutorId, null, request.takeoverTutorId(), request.enabled());
                if (isNew) created++;
                else updated++;
            }
        } else {
            for (Long accountId : accountIds) {
                WecomAccount account = wecomAccountService.require(accountId);
                if (!tutorIds.contains(account.getTutorId())) {
                    throw new IllegalArgumentException("账号不属于已选辅导: " + account.getAccountName());
                }
                boolean isNew = saveOneIfNeeded(
                        account.getTutorId(), accountId, request.takeoverTutorId(), request.enabled());
                if (isNew) created++;
                else updated++;
            }
        }
        return new BatchTakeoverResultVO(created, updated, created + updated);
    }

    private boolean saveOneIfNeeded(Long sourceTutorId, Long wecomAccountId, Long takeoverTutorId, Boolean enabled) {
        Optional<TakeoverAssignment> existing = assignmentRepository
                .findBySourceTutorIdAndWecomAccountIdAndTakeoverTutorId(sourceTutorId, wecomAccountId, takeoverTutorId);
        TakeoverAssignment entity = existing.orElse(new TakeoverAssignment());
        boolean isNew = entity.getId() == null;
        entity.setSourceTutorId(sourceTutorId);
        entity.setWecomAccountId(wecomAccountId);
        entity.setTakeoverTutorId(takeoverTutorId);
        entity.setEnabled(enabled);
        assignmentRepository.save(entity);
        return isNew;
    }

    private TakeoverAssignment saveOne(Long sourceTutorId, Long wecomAccountId, Long takeoverTutorId, Boolean enabled) {
        saveOneIfNeeded(sourceTutorId, wecomAccountId, takeoverTutorId, enabled);
        return assignmentRepository
                .findBySourceTutorIdAndWecomAccountIdAndTakeoverTutorId(sourceTutorId, wecomAccountId, takeoverTutorId)
                .orElseThrow();
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    private Map<Long, String> buildTutorOrgPathMap() {
        return orgService.getOrgTree().stream()
                .flatMap(this::flattenTutors)
                .collect(Collectors.toMap(TutorOrgVO::id, TutorOrgVO::orgPath, (a, b) -> a));
    }

    private Stream<TutorOrgVO> flattenTutors(OrgTreeNodeVO node) {
        Stream<TutorOrgVO> current = node.tutors() != null ? node.tutors().stream() : Stream.empty();
        Stream<TutorOrgVO> childTutors = node.children() != null
                ? node.children().stream().flatMap(this::flattenTutors)
                : Stream.empty();
        return Stream.concat(current, childTutors);
    }

    private TakeoverAssignmentVO toVO(
            TakeoverAssignment a,
            Map<Long, String> tutorNames,
            Map<Long, WecomAccount> accountMap,
            Map<Long, String> tutorOrgPaths) {
        WecomAccount account = a.getWecomAccountId() != null ? accountMap.get(a.getWecomAccountId()) : null;
        String grade = account != null ? account.getGradeLevel() : null;
        String subject = account != null ? account.getSubject() : null;
        String accountName = a.getWecomAccountId() == null
                ? "全部账号"
                : (account != null ? account.getAccountName() : "");
        if (account != null && (grade != null || subject != null)) {
            String extra = (grade != null ? grade : "") + (grade != null && subject != null ? "·" : "") + (subject != null ? subject : "");
            accountName = account.getAccountName() + "（" + extra + "）";
        }
        return new TakeoverAssignmentVO(
                a.getId(),
                a.getSourceTutorId(),
                tutorNames.getOrDefault(a.getSourceTutorId(), ""),
                a.getWecomAccountId(),
                accountName,
                a.getWecomAccountId() == null,
                a.getTakeoverTutorId(),
                tutorNames.getOrDefault(a.getTakeoverTutorId(), ""),
                grade,
                subject,
                tutorOrgPaths.getOrDefault(a.getSourceTutorId(), ""),
                a.getEnabled());
    }
}
