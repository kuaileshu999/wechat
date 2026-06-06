package com.wechat.hosting.service;

import com.wechat.hosting.common.PageResult;
import com.wechat.hosting.dto.ConversationVO;
import com.wechat.hosting.dto.TransferConversationRequest;
import com.wechat.hosting.dto.WorkbenchStatsVO;
import com.wechat.hosting.entity.*;
import com.wechat.hosting.enums.ConvType;
import com.wechat.hosting.enums.StudentStage;
import com.wechat.hosting.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private static final int ACTION_TRANSFER = 3;

    private final ConversationRepository conversationRepository;
    private final ConversationHandlerRepository conversationHandlerRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final TutorRepository tutorRepository;
    private final StudentRepository studentRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final SysUserRepository sysUserRepository;
    private final HostingAssignmentRepository hostingAssignmentRepository;
    private final HostingTransferLogRepository hostingTransferLogRepository;
    private final TakeoverManagerRepository takeoverManagerRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationHandlerRepository conversationHandlerRepository,
                               MessageReadStatusRepository messageReadStatusRepository,
                               TutorWechatAccountRepository accountRepository,
                               TutorRepository tutorRepository,
                               StudentRepository studentRepository,
                               ChatGroupRepository chatGroupRepository,
                               SysUserRepository sysUserRepository,
                               HostingAssignmentRepository hostingAssignmentRepository,
                               HostingTransferLogRepository hostingTransferLogRepository,
                               TakeoverManagerRepository takeoverManagerRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationHandlerRepository = conversationHandlerRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
        this.accountRepository = accountRepository;
        this.tutorRepository = tutorRepository;
        this.studentRepository = studentRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.sysUserRepository = sysUserRepository;
        this.hostingAssignmentRepository = hostingAssignmentRepository;
        this.hostingTransferLogRepository = hostingTransferLogRepository;
        this.takeoverManagerRepository = takeoverManagerRepository;
    }

    public PageResult<ConversationVO> list(Long handlerUserId, Integer stage, Integer convType,
                                           Long tutorId, String keyword, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);

        List<Long> accountIds = resolveAccountIds(tutorId);
        if (tutorId != null && accountIds.isEmpty()) {
            return new PageResult<>(List.of(), 0, safePage, safeSize);
        }

        Set<Long> handledIds = null;
        if (handlerUserId != null) {
            handledIds = conversationHandlerRepository.findByHandlerUserId(handlerUserId)
                    .stream().map(ConversationHandler::getConversationId).collect(Collectors.toSet());
            if (handledIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, safePage, safeSize);
            }
        }

        final Set<Long> finalHandledIds = handledIds;
        Specification<Conversation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 1));
            if (stage != null) {
                predicates.add(cb.equal(root.get("stage"), stage));
            }
            if (convType != null) {
                predicates.add(cb.equal(root.get("convType"), convType));
            }
            if (!accountIds.isEmpty()) {
                predicates.add(root.get("tutorAccountId").in(accountIds));
            }
            if (finalHandledIds != null) {
                predicates.add(root.get("id").in(finalHandledIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Conversation> result = conversationRepository.findAll(
                spec, PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "lastMessageAt")));

        List<Conversation> conversations = result.getContent();

        List<ConversationVO> list = toVOList(conversations, handlerUserId, keyword);
        return new PageResult<>(list, result.getTotalElements(), safePage, safeSize);
    }

    public ConversationVO getDetail(Long conversationId, Long readerUserId) {
        Conversation conversation = requireConversation(conversationId);
        markRead(conversationId, readerUserId);
        return toVOList(List.of(conversation), readerUserId, null).get(0);
    }

    public WorkbenchStatsVO stats(Long handlerUserId) {
        List<ConversationHandler> handlers = conversationHandlerRepository.findByHandlerUserId(handlerUserId);
        Set<Long> convIds = handlers.stream().map(ConversationHandler::getConversationId).collect(Collectors.toSet());
        List<Conversation> conversations = conversationRepository.findAllById(convIds);

        long unread = messageReadStatusRepository.sumUnreadByReaderUserId(handlerUserId);
        int conversion = (int) conversations.stream().filter(c -> Objects.equals(c.getStage(), StudentStage.CONVERSION)).count();
        int undertaking = (int) conversations.stream().filter(c -> Objects.equals(c.getStage(), StudentStage.UNDERTAKING)).count();
        int completed = (int) conversations.stream().filter(c -> Objects.equals(c.getStage(), StudentStage.COMPLETED)).count();
        int tutorCount = takeoverManagerRepository.findByUserId(handlerUserId)
                .map(m -> hostingAssignmentRepository.findByTakeoverManagerIdAndStatus(m.getId(), 1).size())
                .orElse(0);

        return new WorkbenchStatsVO(tutorCount, unread, conversion, undertaking, completed);
    }

    @Transactional
    public ConversationVO transfer(Long conversationId, TransferConversationRequest request) {
        Conversation conversation = requireConversation(conversationId);
        ConversationHandler handler = conversationHandlerRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("会话处理人未配置"));
        sysUserRepository.findById(request.toHandlerUserId())
                .orElseThrow(() -> new IllegalArgumentException("目标处理人不存在"));

        Long fromUserId = handler.getHandlerUserId();
        handler.setHandlerUserId(request.toHandlerUserId());
        handler.setAssignedAt(LocalDateTime.now());
        handler.setHandlerType(1);
        conversationHandlerRepository.save(handler);

        TutorWechatAccount account = accountRepository.findById(conversation.getTutorAccountId())
                .orElseThrow(() -> new IllegalArgumentException("企微账号不存在"));
        HostingTransferLog log = new HostingTransferLog();
        log.setTutorId(account.getTutorId());
        log.setConversationId(conversationId);
        log.setFromHandlerUserId(fromUserId);
        log.setToHandlerUserId(request.toHandlerUserId());
        log.setActionType(ACTION_TRANSFER);
        log.setOperatorId(request.operatorId());
        log.setRemark(request.remark());
        hostingTransferLogRepository.save(log);

        return getDetail(conversationId, request.toHandlerUserId());
    }

    private void markRead(Long conversationId, Long readerUserId) {
        if (readerUserId == null) {
            return;
        }
        MessageReadStatus status = messageReadStatusRepository
                .findByConversationIdAndReaderUserId(conversationId, readerUserId)
                .orElseGet(() -> {
                    MessageReadStatus s = new MessageReadStatus();
                    s.setConversationId(conversationId);
                    s.setReaderUserId(readerUserId);
                    return s;
                });
        Conversation conversation = requireConversation(conversationId);
        status.setLastReadMessageId(conversation.getLastMessageId());
        status.setUnreadCount(0);
        messageReadStatusRepository.save(status);
    }

    private List<Long> resolveAccountIds(Long tutorId) {
        if (tutorId == null) {
            return List.of();
        }
        return accountRepository.findByTutorIdAndStatus(tutorId, 1)
                .stream().map(TutorWechatAccount::getId).toList();
    }

    private Conversation requireConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
    }

    private List<ConversationVO> toVOList(List<Conversation> conversations, Long readerUserId, String keyword) {
        if (conversations.isEmpty()) {
            return List.of();
        }
        Map<Long, TutorWechatAccount> accountMap = accountRepository.findAllById(
                conversations.stream().map(Conversation::getTutorAccountId).distinct().toList())
                .stream().collect(Collectors.toMap(TutorWechatAccount::getId, a -> a));
        Map<Long, Tutor> tutorMap = tutorRepository.findAllById(
                accountMap.values().stream().map(TutorWechatAccount::getTutorId).distinct().toList())
                .stream().collect(Collectors.toMap(Tutor::getId, t -> t));
        Map<Long, SysUser> userMap = sysUserRepository.findByIdIn(
                tutorMap.values().stream().map(Tutor::getUserId).toList())
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, Student> studentMap = studentRepository.findAllById(
                conversations.stream().map(Conversation::getStudentId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, ChatGroup> groupMap = chatGroupRepository.findByIdIn(
                conversations.stream().map(Conversation::getGroupId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ChatGroup::getId, g -> g));
        Map<Long, ConversationHandler> handlerMap = conversations.stream()
                .map(c -> conversationHandlerRepository.findByConversationId(c.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ConversationHandler::getConversationId, h -> h, (a, b) -> a));
        Map<Long, Integer> unreadMap = readerUserId == null ? Map.of() :
                messageReadStatusRepository.findByReaderUserId(readerUserId).stream()
                        .collect(Collectors.toMap(MessageReadStatus::getConversationId,
                                MessageReadStatus::getUnreadCount, (a, b) -> a));

        return conversations.stream()
                .map(c -> toVO(c, accountMap, tutorMap, userMap, studentMap, groupMap, handlerMap, unreadMap))
                .filter(vo -> keyword == null || keyword.isBlank()
                        || containsKeyword(vo, keyword))
                .toList();
    }

    private boolean containsKeyword(ConversationVO vo, String keyword) {
        String k = keyword.toLowerCase();
        return (vo.studentName() != null && vo.studentName().toLowerCase().contains(k))
                || (vo.groupName() != null && vo.groupName().toLowerCase().contains(k));
    }

    private ConversationVO toVO(Conversation c, Map<Long, TutorWechatAccount> accountMap,
                                Map<Long, Tutor> tutorMap, Map<Long, SysUser> userMap,
                                Map<Long, Student> studentMap, Map<Long, ChatGroup> groupMap,
                                Map<Long, ConversationHandler> handlerMap, Map<Long, Integer> unreadMap) {
        TutorWechatAccount account = accountMap.get(c.getTutorAccountId());
        Tutor tutor = account != null ? tutorMap.get(account.getTutorId()) : null;
        SysUser tutorUser = tutor != null ? userMap.get(tutor.getUserId()) : null;
        Student student = c.getStudentId() != null ? studentMap.get(c.getStudentId()) : null;
        ChatGroup group = c.getGroupId() != null ? groupMap.get(c.getGroupId()) : null;
        ConversationHandler handler = handlerMap.get(c.getId());
        SysUser handlerUser = handler != null ? sysUserRepository.findById(handler.getHandlerUserId()).orElse(null) : null;

        return new ConversationVO(
                c.getId(),
                c.getConvType(),
                c.getConvType() == ConvType.PRIVATE ? "私聊" : "群聊",
                tutor != null ? tutor.getId() : null,
                tutorUser != null ? tutorUser.getRealName() : "",
                account != null ? account.getId() : null,
                account != null ? account.getAccountName() : "",
                account != null ? account.getSubject() : "",
                account != null ? account.getGrade() : "",
                student != null ? student.getId() : null,
                student != null ? student.getNickname() : (group != null ? group.getGroupName() : ""),
                student != null ? student.getAvatarUrl() : null,
                group != null ? group.getId() : null,
                group != null ? group.getGroupName() : null,
                c.getStage(),
                c.getStage() != null ? StudentStage.label(c.getStage()) : "",
                c.getLastMessagePreview(),
                c.getLastMessageAt(),
                unreadMap.getOrDefault(c.getId(), 0),
                handler != null ? handler.getHandlerUserId() : null,
                handlerUser != null ? handlerUser.getRealName() : ""
        );
    }
}
