package com.wechat.hosting.service;

import com.wechat.hosting.dto.*;
import com.wechat.hosting.entity.*;
import com.wechat.hosting.enums.HostingStatus;
import com.wechat.hosting.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HostingConfigService {

    private static final int ASSIGNMENT_ACTIVE = 1;
    private static final int EFFECTIVE_IMMEDIATE = 1;
    private static final int EFFECTIVE_SCHEDULED = 2;
    private static final int CONFIG_TUTOR_PENDING = 1;
    private static final int CONFIG_TUTOR_SKIPPED = 0;
    private static final int CONFIG_TUTOR_ACTIVE = 2;
    private static final int ACTION_START = 1;

    private final HostingConfigRepository hostingConfigRepository;
    private final HostingConfigTutorRepository hostingConfigTutorRepository;
    private final HostingAssignmentRepository hostingAssignmentRepository;
    private final HostingTransferLogRepository hostingTransferLogRepository;
    private final TakeoverManagerRepository takeoverManagerRepository;
    private final TutorRepository tutorRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationHandlerRepository conversationHandlerRepository;
    private final SysUserRepository sysUserRepository;
    private final TeachingGroupRepository teachingGroupRepository;

    public HostingConfigService(HostingConfigRepository hostingConfigRepository,
                                HostingConfigTutorRepository hostingConfigTutorRepository,
                                HostingAssignmentRepository hostingAssignmentRepository,
                                HostingTransferLogRepository hostingTransferLogRepository,
                                TakeoverManagerRepository takeoverManagerRepository,
                                TutorRepository tutorRepository,
                                TutorWechatAccountRepository accountRepository,
                                ConversationRepository conversationRepository,
                                ConversationHandlerRepository conversationHandlerRepository,
                                SysUserRepository sysUserRepository,
                                TeachingGroupRepository teachingGroupRepository) {
        this.hostingConfigRepository = hostingConfigRepository;
        this.hostingConfigTutorRepository = hostingConfigTutorRepository;
        this.hostingAssignmentRepository = hostingAssignmentRepository;
        this.hostingTransferLogRepository = hostingTransferLogRepository;
        this.takeoverManagerRepository = takeoverManagerRepository;
        this.tutorRepository = tutorRepository;
        this.accountRepository = accountRepository;
        this.conversationRepository = conversationRepository;
        this.conversationHandlerRepository = conversationHandlerRepository;
        this.sysUserRepository = sysUserRepository;
        this.teachingGroupRepository = teachingGroupRepository;
    }

    public List<HostingConfigVO> listAll() {
        return hostingConfigRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(HostingStatus.DRAFT, HostingStatus.PENDING, HostingStatus.ACTIVE,
                                HostingStatus.ENDED, HostingStatus.CANCELLED))
                .stream().map(this::toVO).toList();
    }

    public HostingConfigVO getById(Long id) {
        HostingConfig config = requireConfig(id);
        return toVO(config);
    }

    @Transactional
    public HostingConfigVO create(CreateHostingConfigRequest request) {
        takeoverManagerRepository.findById(request.takeoverManagerId())
                .orElseThrow(() -> new IllegalArgumentException("接管者不存在"));
        sysUserRepository.findById(request.createdBy())
                .orElseThrow(() -> new IllegalArgumentException("创建人不存在"));

        HostingConfig config = new HostingConfig();
        config.setTakeoverManagerId(request.takeoverManagerId());
        config.setEffectiveType(request.effectiveType());
        config.setScheduledStartAt(request.scheduledStartAt());
        config.setDescription(request.description());
        config.setCreatedBy(request.createdBy());
        if (Objects.equals(request.effectiveType(), EFFECTIVE_IMMEDIATE)) {
            config.setStatus(HostingStatus.PENDING);
        } else {
            if (request.scheduledStartAt() == null) {
                throw new IllegalArgumentException("定时生效必须填写生效时间");
            }
            config.setStatus(HostingStatus.PENDING);
        }
        hostingConfigRepository.save(config);

        for (Long tutorId : request.tutorIds()) {
            tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new IllegalArgumentException("辅导老师不存在: " + tutorId));
            HostingConfigTutor item = new HostingConfigTutor();
            item.setHostingConfigId(config.getId());
            item.setTutorId(tutorId);
            if (hostingAssignmentRepository.existsByTutorIdAndStatus(tutorId, ASSIGNMENT_ACTIVE)) {
                item.setStatus(CONFIG_TUTOR_SKIPPED);
                item.setSkipReason("该辅导老师已被他人托管");
            } else {
                item.setStatus(CONFIG_TUTOR_PENDING);
            }
            hostingConfigTutorRepository.save(item);
        }

        if (Objects.equals(request.effectiveType(), EFFECTIVE_IMMEDIATE)) {
            activate(config.getId(), request.createdBy());
            config = requireConfig(config.getId());
        }
        return toVO(config);
    }

    @Transactional
    public HostingConfigVO activate(Long configId, Long operatorId) {
        HostingConfig config = requireConfig(configId);
        if (config.getStatus() == HostingStatus.ACTIVE) {
            return toVO(config);
        }
        if (config.getStatus() == HostingStatus.ENDED || config.getStatus() == HostingStatus.CANCELLED) {
            throw new IllegalArgumentException("配置已结束或已取消");
        }

        TakeoverManager manager = takeoverManagerRepository.findById(config.getTakeoverManagerId())
                .orElseThrow(() -> new IllegalArgumentException("接管者不存在"));
        SysUser managerUser = sysUserRepository.findById(manager.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("接管者用户不存在"));

        List<HostingConfigTutor> items = hostingConfigTutorRepository.findByHostingConfigId(configId);
        for (HostingConfigTutor item : items) {
            if (item.getStatus() == CONFIG_TUTOR_SKIPPED) {
                continue;
            }
            if (hostingAssignmentRepository.existsByTutorIdAndStatus(item.getTutorId(), ASSIGNMENT_ACTIVE)) {
                item.setStatus(CONFIG_TUTOR_SKIPPED);
                item.setSkipReason("该辅导老师已被他人托管");
                hostingConfigTutorRepository.save(item);
                continue;
            }

            HostingAssignment assignment = new HostingAssignment();
            assignment.setHostingConfigId(configId);
            assignment.setTutorId(item.getTutorId());
            assignment.setTakeoverManagerId(config.getTakeoverManagerId());
            assignment.setStartedAt(LocalDateTime.now());
            assignment.setStatus(ASSIGNMENT_ACTIVE);
            hostingAssignmentRepository.save(assignment);

            initConversationHandlers(item.getTutorId(), assignment.getId(), managerUser.getId());

            item.setStatus(CONFIG_TUTOR_ACTIVE);
            hostingConfigTutorRepository.save(item);

            HostingTransferLog log = new HostingTransferLog();
            log.setTutorId(item.getTutorId());
            log.setActionType(ACTION_START);
            log.setToHandlerUserId(managerUser.getId());
            log.setOperatorId(operatorId);
            log.setRemark("开始托管");
            hostingTransferLogRepository.save(log);
        }

        config.setStatus(HostingStatus.ACTIVE);
        hostingConfigRepository.save(config);
        return toVO(config);
    }

    @Transactional
    public HostingConfigVO end(Long configId, Long operatorId) {
        HostingConfig config = requireConfig(configId);
        if (config.getStatus() != HostingStatus.ACTIVE) {
            throw new IllegalArgumentException("仅生效中的配置可以结束");
        }

        List<HostingAssignment> assignments = hostingAssignmentRepository
                .findByTakeoverManagerIdAndStatus(config.getTakeoverManagerId(), ASSIGNMENT_ACTIVE)
                .stream()
                .filter(a -> Objects.equals(a.getHostingConfigId(), configId))
                .toList();

        for (HostingAssignment assignment : assignments) {
            assignment.setStatus(2);
            assignment.setEndedAt(LocalDateTime.now());
            hostingAssignmentRepository.save(assignment);

            Tutor tutor = tutorRepository.findById(assignment.getTutorId()).orElse(null);
            if (tutor != null) {
                restoreHandlersToTutor(assignment.getId(), tutor.getUserId());
                HostingTransferLog log = new HostingTransferLog();
                log.setTutorId(assignment.getTutorId());
                log.setActionType(2);
                log.setOperatorId(operatorId);
                log.setRemark("结束托管");
                hostingTransferLogRepository.save(log);
            }
        }

        config.setStatus(HostingStatus.ENDED);
        hostingConfigRepository.save(config);
        return toVO(config);
    }

    @Transactional
    public void processScheduledConfigs() {
        List<HostingConfig> pending = hostingConfigRepository
                .findByStatusAndEffectiveTypeAndScheduledStartAtLessThanEqual(
                        HostingStatus.PENDING, EFFECTIVE_SCHEDULED, LocalDateTime.now());
        for (HostingConfig config : pending) {
            activate(config.getId(), config.getCreatedBy());
        }
    }

    private void initConversationHandlers(Long tutorId, Long assignmentId, Long handlerUserId) {
        List<Long> accountIds = accountRepository.findByTutorIdAndStatus(tutorId, 1)
                .stream().map(TutorWechatAccount::getId).toList();
        if (accountIds.isEmpty()) {
            return;
        }
        List<Conversation> conversations = conversationRepository
                .findByTutorAccountIdInAndStatus(accountIds, 1);
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

    private HostingConfig requireConfig(Long id) {
        return hostingConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("托管配置不存在"));
    }

    private HostingConfigVO toVO(HostingConfig config) {
        TakeoverManager manager = takeoverManagerRepository.findById(config.getTakeoverManagerId()).orElse(null);
        String managerName = "";
        if (manager != null) {
            managerName = sysUserRepository.findById(manager.getUserId())
                    .map(SysUser::getRealName).orElse("");
        }
        List<HostingConfigTutor> items = hostingConfigTutorRepository.findByHostingConfigId(config.getId());
        Map<Long, Tutor> tutorMap = tutorRepository.findAllById(
                items.stream().map(HostingConfigTutor::getTutorId).toList())
                .stream().collect(Collectors.toMap(Tutor::getId, t -> t));
        Map<Long, SysUser> userMap = sysUserRepository.findByIdIn(
                tutorMap.values().stream().map(Tutor::getUserId).toList())
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, TeachingGroup> groupMap = teachingGroupRepository.findAllById(
                tutorMap.values().stream().map(Tutor::getTeachingGroupId).toList())
                .stream().collect(Collectors.toMap(TeachingGroup::getId, g -> g));

        List<HostingConfigTutorVO> tutorVOs = items.stream().map(item -> {
            Tutor tutor = tutorMap.get(item.getTutorId());
            String tutorName = "";
            String groupName = "";
            if (tutor != null) {
                SysUser user = userMap.get(tutor.getUserId());
                tutorName = user != null ? user.getRealName() : "";
                TeachingGroup group = groupMap.get(tutor.getTeachingGroupId());
                groupName = group != null ? group.getName() : "";
            }
            return new HostingConfigTutorVO(item.getTutorId(), tutorName, groupName,
                    item.getStatus(), item.getSkipReason());
        }).toList();

        return new HostingConfigVO(
                config.getId(), config.getTakeoverManagerId(), managerName,
                config.getEffectiveType(), config.getScheduledStartAt(), config.getDescription(),
                config.getStatus(), config.getCreatedBy(), config.getCreatedAt(), tutorVOs);
    }
}
