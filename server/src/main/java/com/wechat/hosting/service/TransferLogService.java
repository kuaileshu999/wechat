package com.wechat.hosting.service;

import com.wechat.hosting.dto.TransferLogVO;
import com.wechat.hosting.entity.*;
import com.wechat.hosting.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransferLogService {

    private final HostingTransferLogRepository hostingTransferLogRepository;
    private final TutorRepository tutorRepository;
    private final SysUserRepository sysUserRepository;
    private final ConversationRepository conversationRepository;
    private final StudentRepository studentRepository;
    private final ChatGroupRepository chatGroupRepository;

    public TransferLogService(HostingTransferLogRepository hostingTransferLogRepository,
                              TutorRepository tutorRepository,
                              SysUserRepository sysUserRepository,
                              ConversationRepository conversationRepository,
                              StudentRepository studentRepository,
                              ChatGroupRepository chatGroupRepository) {
        this.hostingTransferLogRepository = hostingTransferLogRepository;
        this.tutorRepository = tutorRepository;
        this.sysUserRepository = sysUserRepository;
        this.conversationRepository = conversationRepository;
        this.studentRepository = studentRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    public List<TransferLogVO> listRecent() {
        return hostingTransferLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream().map(this::toVO).toList();
    }

    public List<TransferLogVO> listByTutor(Long tutorId) {
        return hostingTransferLogRepository.findByTutorIdOrderByCreatedAtDesc(tutorId)
                .stream().map(this::toVO).toList();
    }

    private TransferLogVO toVO(HostingTransferLog log) {
        Map<Long, SysUser> userMap = sysUserRepository.findByIdIn(
                List.of(log.getFromHandlerUserId(), log.getToHandlerUserId(), log.getOperatorId())
                        .stream().filter(id -> id != null).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        String tutorName = tutorRepository.findById(log.getTutorId())
                .flatMap(t -> sysUserRepository.findById(t.getUserId()))
                .map(SysUser::getRealName).orElse("");
        String conversationTitle = "";
        if (log.getConversationId() != null) {
            conversationTitle = conversationRepository.findById(log.getConversationId())
                    .map(c -> {
                        if (c.getStudentId() != null) {
                            return studentRepository.findById(c.getStudentId())
                                    .map(Student::getNickname).orElse("");
                        }
                        if (c.getGroupId() != null) {
                            return chatGroupRepository.findById(c.getGroupId())
                                    .map(ChatGroup::getGroupName).orElse("");
                        }
                        return "";
                    }).orElse("");
        }
        return new TransferLogVO(
                log.getId(),
                log.getTutorId(),
                tutorName,
                log.getConversationId(),
                conversationTitle,
                log.getFromHandlerUserId(),
                name(userMap, log.getFromHandlerUserId()),
                log.getToHandlerUserId(),
                name(userMap, log.getToHandlerUserId()),
                log.getActionType(),
                actionLabel(log.getActionType()),
                log.getOperatorId(),
                name(userMap, log.getOperatorId()),
                log.getRemark(),
                log.getCreatedAt()
        );
    }

    private String name(Map<Long, SysUser> userMap, Long userId) {
        if (userId == null) {
            return "";
        }
        SysUser user = userMap.get(userId);
        return user != null ? user.getRealName() : "";
    }

    private String actionLabel(Integer actionType) {
        return switch (actionType) {
            case 1 -> "开始托管";
            case 2 -> "结束托管";
            case 3 -> "转接聊天";
            default -> "未知";
        };
    }
}
