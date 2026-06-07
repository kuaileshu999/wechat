package com.wechat.hosting.service;

import com.wechat.hosting.common.PageResult;
import com.wechat.hosting.dto.HostingAssignmentStatsVO;
import com.wechat.hosting.dto.HostingAssignmentVO;
import com.wechat.hosting.entity.*;
import com.wechat.hosting.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HostingAssignmentService {

    private static final int ASSIGNMENT_ACTIVE = 1;
    private static final int ASSIGNMENT_ENDED = 2;
    private static final int ACTION_START = 1;
    private static final int ACTION_END = 2;

    private final HostingAssignmentRepository assignmentRepository;
    private final HostingConfigRepository hostingConfigRepository;
    private final TutorRepository tutorRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final TakeoverManagerRepository takeoverManagerRepository;
    private final SysUserRepository sysUserRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationHandlerRepository conversationHandlerRepository;
    private final HostingTransferLogRepository hostingTransferLogRepository;
    private final MessageRepository messageRepository;

    public HostingAssignmentService(HostingAssignmentRepository assignmentRepository,
                                    HostingConfigRepository hostingConfigRepository,
                                    TutorRepository tutorRepository,
                                    TutorWechatAccountRepository accountRepository,
                                    TakeoverManagerRepository takeoverManagerRepository,
                                    SysUserRepository sysUserRepository,
                                    TeachingGroupRepository teachingGroupRepository,
                                    ConversationRepository conversationRepository,
                                    ConversationHandlerRepository conversationHandlerRepository,
                                    HostingTransferLogRepository hostingTransferLogRepository,
                                    MessageRepository messageRepository) {
        this.assignmentRepository = assignmentRepository;
        this.hostingConfigRepository = hostingConfigRepository;
        this.tutorRepository = tutorRepository;
        this.accountRepository = accountRepository;
        this.takeoverManagerRepository = takeoverManagerRepository;
        this.sysUserRepository = sysUserRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.conversationRepository = conversationRepository;
        this.conversationHandlerRepository = conversationHandlerRepository;
        this.hostingTransferLogRepository = hostingTransferLogRepository;
        this.messageRepository = messageRepository;
    }

    public PageResult<HostingAssignmentVO> list(String keyword, Integer status, Long accountId,
                                                  int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Specification<HostingAssignment> spec = buildSpec(keyword, status, accountId);
        Page<HostingAssignment> result = assignmentRepository.findAll(
                spec,
                PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "startedAt")));
        List<HostingAssignmentVO> list = result.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotalElements(), safePage, safeSize);
    }

    public HostingAssignmentStatsVO stats() {
        long activeCount = assignmentRepository.countByStatus(ASSIGNMENT_ACTIVE);
        long tutorCount = tutorRepository.countByStatus(1);
        long managerCount = takeoverManagerRepository.countByStatus(1);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long todayMessageCount = messageRepository.countBySentAtBetween(startOfDay, endOfDay);
        return new HostingAssignmentStatsVO(activeCount, tutorCount, managerCount, todayMessageCount);
    }

    @Transactional
    public HostingAssignmentVO release(Long assignmentId, Long operatorId) {
        HostingAssignment assignment = requireAssignment(assignmentId);
        if (assignment.getStatus() != ASSIGNMENT_ACTIVE) {
            throw new IllegalArgumentException("仅进行中的托管可以解除");
        }
        endAssignment(assignment, operatorId, "解除接管");
        return toVO(assignment);
    }

    @Transactional
    public HostingAssignmentVO reactivate(Long assignmentId, Long operatorId) {
        HostingAssignment old = requireAssignment(assignmentId);
        if (old.getStatus() != ASSIGNMENT_ENDED) {
            throw new IllegalArgumentException("仅已解除的托管可以重新接管");
        }
        if (old.getTutorAccountId() == null) {
            throw new IllegalArgumentException("缺少企微账号信息，无法重新接管");
        }
        if (assignmentRepository.existsByTutorAccountIdAndStatus(old.getTutorAccountId(), ASSIGNMENT_ACTIVE)) {
            throw new IllegalArgumentException("该企微账号已被他人托管");
        }

        TakeoverManager manager = takeoverManagerRepository.findById(old.getTakeoverManagerId())
                .orElseThrow(() -> new IllegalArgumentException("接管者不存在"));
        SysUser managerUser = sysUserRepository.findById(manager.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("接管者用户不存在"));

        HostingAssignment assignment = new HostingAssignment();
        assignment.setHostingConfigId(old.getHostingConfigId());
        assignment.setTutorId(old.getTutorId());
        assignment.setTutorAccountId(old.getTutorAccountId());
        assignment.setTakeoverManagerId(old.getTakeoverManagerId());
        assignment.setStartedAt(LocalDateTime.now());
        assignment.setStatus(ASSIGNMENT_ACTIVE);
        assignmentRepository.save(assignment);

        initConversationHandlers(old.getTutorAccountId(), assignment.getId(), managerUser.getId());

        HostingTransferLog log = new HostingTransferLog();
        log.setTutorId(old.getTutorId());
        log.setActionType(ACTION_START);
        log.setToHandlerUserId(managerUser.getId());
        log.setOperatorId(operatorId);
        log.setRemark("重新接管");
        hostingTransferLogRepository.save(log);

        return toVO(assignment);
    }

    public Set<Long> findActiveHostedAccountIds() {
        return assignmentRepository.findByStatus(ASSIGNMENT_ACTIVE).stream()
                .map(HostingAssignment::getTutorAccountId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void endAssignment(HostingAssignment assignment, Long operatorId, String remark) {
        assignment.setStatus(ASSIGNMENT_ENDED);
        assignment.setEndedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        Tutor tutor = tutorRepository.findById(assignment.getTutorId()).orElse(null);
        if (tutor != null) {
            restoreHandlersToTutor(assignment.getId(), tutor.getUserId());
            HostingTransferLog log = new HostingTransferLog();
            log.setTutorId(assignment.getTutorId());
            log.setActionType(ACTION_END);
            log.setOperatorId(operatorId);
            log.setRemark(remark);
            hostingTransferLogRepository.save(log);
        }
    }

    private void initConversationHandlers(Long accountId, Long assignmentId, Long handlerUserId) {
        List<Conversation> conversations = conversationRepository
                .findByTutorAccountIdInAndStatus(List.of(accountId), 1);
        for (Conversation conversation : conversations) {
            ConversationHandler handler = conversationHandlerRepository
                    .findByConversationId(conversation.getId())
                    .orElse(new ConversationHandler());
            handler.setConversationId(conversation.getId());
            handler.setHandlerUserId(handlerUserId);
            handler.setHandlerType(1);
            handler.setHostingAssignmentId(assignmentId);
            handler.setAssignedAt(LocalDateTime.now());
            conversationHandlerRepository.save(handler);
        }
    }

    private void restoreHandlersToTutor(Long assignmentId, Long tutorUserId) {
        List<ConversationHandler> handlers = conversationHandlerRepository
                .findByHostingAssignmentId(assignmentId);
        for (ConversationHandler handler : handlers) {
            handler.setHandlerUserId(tutorUserId);
            handler.setHandlerType(2);
            handler.setHostingAssignmentId(null);
            handler.setAssignedAt(LocalDateTime.now());
            conversationHandlerRepository.save(handler);
        }
    }

    private Specification<HostingAssignment> buildSpec(String keyword, Integer status, Long accountId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (accountId != null) {
                predicates.add(cb.equal(root.get("tutorAccountId"), accountId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.trim();
                List<Long> tutorIds = tutorRepository.findByStatus(1).stream()
                        .filter(t -> matchTutorKeyword(t, kw))
                        .map(Tutor::getId)
                        .toList();
                List<Long> managerIds = takeoverManagerRepository.findByStatus(1).stream()
                        .filter(m -> matchManagerKeyword(m, kw))
                        .map(TakeoverManager::getId)
                        .toList();
                List<Long> accountIds = accountRepository.findAll().stream()
                        .filter(a -> a.getAccountName() != null && a.getAccountName().contains(kw))
                        .map(TutorWechatAccount::getId)
                        .toList();
                Predicate tutorMatch = tutorIds.isEmpty() ? cb.disjunction() : root.get("tutorId").in(tutorIds);
                Predicate managerMatch = managerIds.isEmpty() ? cb.disjunction() : root.get("takeoverManagerId").in(managerIds);
                Predicate accountMatch = accountIds.isEmpty() ? cb.disjunction() : root.get("tutorAccountId").in(accountIds);
                predicates.add(cb.or(tutorMatch, managerMatch, accountMatch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean matchTutorKeyword(Tutor tutor, String kw) {
        SysUser user = sysUserRepository.findById(tutor.getUserId()).orElse(null);
        if (user == null) {
            return false;
        }
        TeachingGroup group = teachingGroupRepository.findById(tutor.getTeachingGroupId()).orElse(null);
        return (user.getRealName() != null && user.getRealName().contains(kw))
                || (group != null && group.getName() != null && group.getName().contains(kw));
    }

    private boolean matchManagerKeyword(TakeoverManager manager, String kw) {
        SysUser user = sysUserRepository.findById(manager.getUserId()).orElse(null);
        return user != null && user.getRealName() != null && user.getRealName().contains(kw);
    }

    private HostingAssignment requireAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("托管记录不存在"));
    }

    private HostingAssignmentVO toVO(HostingAssignment assignment) {
        Tutor tutor = tutorRepository.findById(assignment.getTutorId()).orElse(null);
        String tutorName = "";
        String groupName = "";
        if (tutor != null) {
            tutorName = sysUserRepository.findById(tutor.getUserId()).map(SysUser::getRealName).orElse("");
            groupName = teachingGroupRepository.findById(tutor.getTeachingGroupId())
                    .map(TeachingGroup::getName).orElse("");
        }
        TutorWechatAccount account = assignment.getTutorAccountId() == null ? null
                : accountRepository.findById(assignment.getTutorAccountId()).orElse(null);
        TakeoverManager manager = takeoverManagerRepository.findById(assignment.getTakeoverManagerId()).orElse(null);
        String managerName = "";
        if (manager != null) {
            managerName = sysUserRepository.findById(manager.getUserId()).map(SysUser::getRealName).orElse("");
        }
        HostingConfig config = hostingConfigRepository.findById(assignment.getHostingConfigId()).orElse(null);
        return new HostingAssignmentVO(
                assignment.getId(),
                assignment.getHostingConfigId(),
                assignment.getTutorId(),
                tutorName,
                groupName,
                assignment.getTutorAccountId(),
                account != null ? account.getAccountName() : "",
                account != null ? account.getWechatUserid() : "",
                assignment.getTakeoverManagerId(),
                managerName,
                assignment.getStartedAt(),
                assignment.getEndedAt(),
                assignment.getStatus(),
                config != null ? config.getDescription() : "",
                config != null ? config.getEffectiveType() : null
        );
    }
}
