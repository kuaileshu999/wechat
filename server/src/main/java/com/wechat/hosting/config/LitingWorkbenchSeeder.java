package com.wechat.hosting.config;

import com.wechat.hosting.entity.*;
import com.wechat.hosting.enums.ConvType;
import com.wechat.hosting.enums.HostingStatus;
import com.wechat.hosting.enums.StudentStage;
import com.wechat.hosting.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 为接管者「李婷」补充 10 个私聊学生会话（按学科+年级分布），并设置未读数。
 */
@Component
@Order(2)
public class LitingWorkbenchSeeder implements CommandLineRunner {

    private record DemoStudent(String nickname, String externalId, String subject, String grade,
                               int stage, String preview, int unread, Long tutorId, int minutesAgo) {
    }

    private static final List<DemoStudent> DEMO_STUDENTS = List.of(
            new DemoStudent("李明", "ext_demo_001", "数学", "高一", StudentStage.CONVERSION, "老师，这道题我还不太懂", 3, 1L, 5),
            new DemoStudent("王芳", "ext_demo_002", "数学", "高一", StudentStage.CONVERSION, "好的，我今晚完成作业", 1, 1L, 20),
            new DemoStudent("孙磊", "ext_demo_003", "数学", "高一", StudentStage.CONVERSION, "函数那块能再讲一遍吗", 2, 1L, 35),
            new DemoStudent("周婷", "ext_demo_004", "数学", "高一", StudentStage.UNDERTAKING, "作业已提交", 0, 1L, 60),
            new DemoStudent("褚伟", "ext_demo_005", "数学", "高一", StudentStage.UNDERTAKING, "下周考试范围是什么", 4, 1L, 90),
            new DemoStudent("赵强", "ext_demo_006", "数学", "高二", StudentStage.CONVERSION, "请问课程资料在哪里下载", 2, 2L, 15),
            new DemoStudent("吴昊", "ext_demo_007", "数学", "高二", StudentStage.CONVERSION, "圆锥曲线不太会", 1, 2L, 40),
            new DemoStudent("钱琳", "ext_demo_008", "数学", "高二", StudentStage.UNDERTAKING, "谢谢老师讲解", 0, 2L, 120),
            new DemoStudent("郑悦", "ext_demo_009", "物理", "高二", StudentStage.CONVERSION, "电磁感应那题求思路", 3, 3L, 25),
            new DemoStudent("冯杰", "ext_demo_010", "物理", "高二", StudentStage.UNDERTAKING, "实验报告怎么写", 2, 3L, 55)
    );

    private final SysUserRepository sysUserRepository;
    private final TakeoverManagerRepository takeoverManagerRepository;
    private final TutorRepository tutorRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationHandlerRepository conversationHandlerRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final HostingConfigRepository hostingConfigRepository;
    private final HostingConfigTutorRepository hostingConfigTutorRepository;
    private final HostingAssignmentRepository hostingAssignmentRepository;

    public LitingWorkbenchSeeder(SysUserRepository sysUserRepository,
                                 TakeoverManagerRepository takeoverManagerRepository,
                                 TutorRepository tutorRepository,
                                 TutorWechatAccountRepository accountRepository,
                                 StudentRepository studentRepository,
                                 ConversationRepository conversationRepository,
                                 ConversationHandlerRepository conversationHandlerRepository,
                                 MessageReadStatusRepository messageReadStatusRepository,
                                 HostingConfigRepository hostingConfigRepository,
                                 HostingConfigTutorRepository hostingConfigTutorRepository,
                                 HostingAssignmentRepository hostingAssignmentRepository) {
        this.sysUserRepository = sysUserRepository;
        this.takeoverManagerRepository = takeoverManagerRepository;
        this.tutorRepository = tutorRepository;
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.conversationRepository = conversationRepository;
        this.conversationHandlerRepository = conversationHandlerRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
        this.hostingConfigRepository = hostingConfigRepository;
        this.hostingConfigTutorRepository = hostingConfigTutorRepository;
        this.hostingAssignmentRepository = hostingAssignmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        SysUser liting = sysUserRepository.findByUsername("liting").orElse(null);
        if (liting == null) {
            return;
        }
        long existing = conversationHandlerRepository.findByHandlerUserId(liting.getId()).size();
        if (existing >= 10) {
            return;
        }

        TakeoverManager manager = takeoverManagerRepository.findByUserId(liting.getId())
                .orElseThrow(() -> new IllegalStateException("李婷接管者记录不存在"));
        SysUser admin = sysUserRepository.findByUsername("admin").orElse(liting);

        ensureHostingForTutors(manager, admin.getId(), List.of(1L, 2L, 3L));

        for (DemoStudent demo : DEMO_STUDENTS) {
            if (studentRepository.findByExternalUserid(demo.externalId()).isPresent()) {
                continue;
            }
            TutorWechatAccount account = accountRepository
                    .findByTutorIdAndStatus(demo.tutorId(), 1).stream()
                    .filter(a -> demo.subject().equals(a.getSubject()) && demo.grade().equals(a.getGrade()))
                    .findFirst()
                    .orElseGet(() -> accountRepository.findByTutorIdAndStatus(demo.tutorId(), 1).stream()
                            .findFirst().orElse(null));
            if (account == null) {
                continue;
            }

            Student student = new Student();
            student.setNickname(demo.nickname());
            student.setExternalUserid(demo.externalId());
            student.setGrade(demo.grade());
            student.setStatus(1);
            studentRepository.save(student);

            Conversation conversation = new Conversation();
            conversation.setTutorAccountId(account.getId());
            conversation.setConvType(ConvType.PRIVATE);
            conversation.setStudentId(student.getId());
            conversation.setStage(demo.stage());
            conversation.setLastMessagePreview(demo.preview());
            conversation.setLastMessageAt(LocalDateTime.now().minusMinutes(demo.minutesAgo()));
            conversation.setStatus(1);
            conversationRepository.save(conversation);

            HostingAssignment assignment = hostingAssignmentRepository
                    .findByTutorIdAndStatus(demo.tutorId(), 1).orElse(null);

            ConversationHandler handler = new ConversationHandler();
            handler.setConversationId(conversation.getId());
            handler.setHandlerUserId(liting.getId());
            handler.setHandlerType(1);
            if (assignment != null) {
                handler.setHostingAssignmentId(assignment.getId());
            }
            handler.setAssignedAt(LocalDateTime.now());
            conversationHandlerRepository.save(handler);

            if (demo.unread() > 0) {
                MessageReadStatus readStatus = new MessageReadStatus();
                readStatus.setConversationId(conversation.getId());
                readStatus.setReaderUserId(liting.getId());
                readStatus.setUnreadCount(demo.unread());
                messageReadStatusRepository.save(readStatus);
            }
        }
    }

    private void ensureHostingForTutors(TakeoverManager manager, Long adminId, List<Long> tutorIds) {
        for (Long tutorId : tutorIds) {
            if (hostingAssignmentRepository.existsByTutorIdAndStatus(tutorId, 1)) {
                continue;
            }
            Optional<HostingConfig> existingConfig = hostingConfigRepository.findAll().stream()
                    .filter(c -> c.getTakeoverManagerId().equals(manager.getId())
                            && c.getStatus() == HostingStatus.ACTIVE)
                    .findFirst();
            HostingConfig config = existingConfig.orElseGet(() -> {
                HostingConfig c = new HostingConfig();
                c.setTakeoverManagerId(manager.getId());
                c.setEffectiveType(1);
                c.setDescription("李婷工作台演示数据");
                c.setStatus(HostingStatus.ACTIVE);
                c.setCreatedBy(adminId);
                return hostingConfigRepository.save(c);
            });

            if (hostingConfigTutorRepository.findByHostingConfigId(config.getId()).stream()
                    .noneMatch(t -> t.getTutorId().equals(tutorId))) {
                HostingConfigTutor ct = new HostingConfigTutor();
                ct.setHostingConfigId(config.getId());
                ct.setTutorId(tutorId);
                ct.setStatus(2);
                hostingConfigTutorRepository.save(ct);
            }

            HostingAssignment assignment = new HostingAssignment();
            assignment.setHostingConfigId(config.getId());
            assignment.setTutorId(tutorId);
            assignment.setTakeoverManagerId(manager.getId());
            assignment.setStartedAt(LocalDateTime.now());
            assignment.setStatus(1);
            hostingAssignmentRepository.save(assignment);
        }
    }
}
