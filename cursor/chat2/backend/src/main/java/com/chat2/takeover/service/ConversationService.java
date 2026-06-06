package com.chat2.takeover.service;

import com.chat2.takeover.dto.ConversationQuery;
import com.chat2.takeover.dto.ConversationVO;
import com.chat2.takeover.dto.PageResult;
import com.chat2.takeover.dto.TutorVO;
import com.chat2.takeover.dto.UnrepliedCountVO;
import com.chat2.takeover.dto.UnrepliedStatsVO;
import com.chat2.takeover.entity.Conversation;
import com.chat2.takeover.entity.Student;
import com.chat2.takeover.entity.WecomAccount;
import com.chat2.takeover.enums.ChatType;
import com.chat2.takeover.enums.MessageCategory;
import com.chat2.takeover.repository.ConversationRepository;
import com.chat2.takeover.repository.StudentRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final StudentRepository studentRepository;
    private final WecomAccountService wecomAccountService;
    private final TutorService tutorService;

    private Map<Long, WecomAccount> accountMap = Map.of();
    private Map<Long, String> tutorNameMap = Map.of();

    public ConversationService(
            ConversationRepository conversationRepository,
            StudentRepository studentRepository,
            WecomAccountService wecomAccountService,
            TutorService tutorService) {
        this.conversationRepository = conversationRepository;
        this.studentRepository = studentRepository;
        this.wecomAccountService = wecomAccountService;
        this.tutorService = tutorService;
    }

    @PostConstruct
    void initCache() {
        refreshCache();
    }

    private void refreshCache() {
        accountMap = wecomAccountService.requireAllMap();
        tutorNameMap = tutorService.listAll().stream()
                .collect(Collectors.toMap(TutorVO::id, TutorVO::name));
    }

    public PageResult<ConversationVO> listPage(ConversationQuery query, int page, int pageSize) {
        ConversationQuery q = query != null ? query : new ConversationQuery();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        PageRequest pageRequest = PageRequest.of(
                safePage - 1,
                safeSize,
                Sort.by(Sort.Direction.DESC, "lastMessageAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Conversation> result = conversationRepository.findAll(buildSpec(q), pageRequest);

        Set<Long> studentIds = result.getContent().stream()
                .map(Conversation::getStudentId)
                .collect(Collectors.toSet());
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Map.of()
                : studentRepository.findByIdIn(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, Function.identity()));

        List<ConversationVO> list = result.getContent().stream()
                .map(c -> toVO(c, studentMap))
                .toList();
        return new PageResult<>(list, result.getTotalElements(), safePage, safeSize);
    }

    public UnrepliedCountVO countUnreplied(MessageCategory category) {
        long privateCount = conversationRepository.countByCategoryAndChatTypeAndPendingReply(
                category, ChatType.PRIVATE, true);
        long groupCount = conversationRepository.countByCategoryAndChatTypeAndPendingReply(
                category, ChatType.GROUP, true);
        return new UnrepliedCountVO(privateCount, groupCount);
    }

    public UnrepliedStatsVO countAllUnreplied() {
        return new UnrepliedStatsVO(
                countUnreplied(MessageCategory.UNDERTAKING),
                countUnreplied(MessageCategory.CONVERSION));
    }

    public ConversationVO getDetail(Long id) {
        Conversation c = require(id);
        Map<Long, Student> studentMap = studentRepository.findByIdIn(List.of(c.getStudentId())).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        return toVO(c, studentMap);
    }

    public Conversation require(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + id));
    }

    private Specification<Conversation> buildSpec(ConversationQuery q) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), q.getCategory()));
            }
            if (q.getChatType() != null) {
                predicates.add(cb.equal(root.get("chatType"), q.getChatType()));
            }
            if (q.getWecomAccountId() != null) {
                predicates.add(cb.equal(root.get("wecomAccountId"), q.getWecomAccountId()));
            }
            if (q.getTutorId() != null) {
                List<Long> accountIds = wecomAccountService.listByTutor(q.getTutorId()).stream()
                        .map(a -> a.id())
                        .toList();
                if (accountIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("wecomAccountId").in(accountIds));
                }
            }
            if (StringUtils.hasText(q.getKeyword())) {
                String like = "%" + q.getKeyword().trim() + "%";
                predicates.add(cb.like(root.get("lastMessagePreview"), like));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ConversationVO toVO(Conversation c, Map<Long, Student> studentMap) {
        WecomAccount account = accountMap.get(c.getWecomAccountId());
        Student student = studentMap.get(c.getStudentId());
        Long tutorId = account != null ? account.getTutorId() : null;
        String tutorName = tutorId != null ? tutorNameMap.getOrDefault(tutorId, "") : "";

        return new ConversationVO(
                c.getId(),
                c.getWecomAccountId(),
                account != null ? account.getAccountName() : "",
                tutorId,
                tutorName,
                c.getStudentId(),
                student != null ? student.getName() : "",
                c.getChatType(),
                c.getChatType() == ChatType.PRIVATE ? "私聊" : "群聊",
                c.getGroupName(),
                c.getCategory(),
                c.getCategory() == MessageCategory.UNDERTAKING ? "承接期" : "转化率",
                c.getLastMessagePreview(),
                c.getLastMessageAt(),
                c.getUnreadCount(),
                Boolean.TRUE.equals(c.getPendingReply()),
                c.getPendingReplyCount() != null ? c.getPendingReplyCount() : 0);
    }
}
