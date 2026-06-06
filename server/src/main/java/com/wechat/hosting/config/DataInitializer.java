package com.wechat.hosting.config;

import com.wechat.hosting.entity.*;
import com.wechat.hosting.enums.ConvType;
import com.wechat.hosting.enums.StudentStage;
import com.wechat.hosting.enums.UserRole;
import com.wechat.hosting.repository.*;
import com.wechat.hosting.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final TutorRepository tutorRepository;
    private final TakeoverManagerRepository takeoverManagerRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ConversationRepository conversationRepository;
    private final AuthService authService;

    public DataInitializer(SysUserRepository sysUserRepository,
                           TeachingGroupRepository teachingGroupRepository,
                           TutorRepository tutorRepository,
                           TakeoverManagerRepository takeoverManagerRepository,
                           TutorWechatAccountRepository accountRepository,
                           StudentRepository studentRepository,
                           ChatGroupRepository chatGroupRepository,
                           ConversationRepository conversationRepository,
                           AuthService authService) {
        this.sysUserRepository = sysUserRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.tutorRepository = tutorRepository;
        this.takeoverManagerRepository = takeoverManagerRepository;
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.conversationRepository = conversationRepository;
        this.authService = authService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (sysUserRepository.count() > 0) {
            return;
        }

        TeachingGroup mathGroup = createGroup("数学教研组", 1);
        TeachingGroup physicsGroup = createGroup("物理教研组", 2);

        SysUser admin = createUser("admin", "管理员", UserRole.ADMIN, "13800000000");
        SysUser managerUser = createUser("liting", "李婷", UserRole.TAKEOVER_MANAGER, "13800000001");
        SysUser tutorUser1 = createUser("zhang", "张明远", UserRole.TUTOR, "13800000002");
        SysUser tutorUser2 = createUser("wang", "王慧", UserRole.TUTOR, "13800000003");
        SysUser tutorUser3 = createUser("liu", "刘洋", UserRole.TUTOR, "13800000004");

        Tutor tutor1 = createTutor(tutorUser1.getId(), mathGroup.getId(), "T001");
        Tutor tutor2 = createTutor(tutorUser2.getId(), mathGroup.getId(), "T002");
        Tutor tutor3 = createTutor(tutorUser3.getId(), physicsGroup.getId(), "T003");

        TakeoverManager manager = new TakeoverManager();
        manager.setUserId(managerUser.getId());
        manager.setMaxTutorCount(10);
        manager.setStatus(1);
        takeoverManagerRepository.save(manager);

        TutorWechatAccount account1 = createAccount(tutor1.getId(), "张老师-数学辅导", "数学", "高一", "wx_zhang_1", 3);
        TutorWechatAccount account2 = createAccount(tutor1.getId(), "张老师-数学答疑", "数学", "高一", "wx_zhang_2", 2);
        TutorWechatAccount account3 = createAccount(tutor2.getId(), "王老师-数学辅导", "数学", "高二", "wx_wang_1", 2);
        TutorWechatAccount account4 = createAccount(tutor3.getId(), "刘老师-物理辅导", "物理", "高二", "wx_liu_1", 2);

        Student s1 = createStudent("李明", "ext_001", "高一");
        Student s2 = createStudent("王芳", "ext_002", "高一");
        Student s3 = createStudent("赵强", "ext_003", "高二");
        Student s4 = createStudent("陈静", "ext_004", "高二");

        ChatGroup group1 = createGroupChat(account4.getId(), "物理冲刺群", "chat_001", 2);

        createConversation(account1.getId(), ConvType.PRIVATE, s1.getId(), null, StudentStage.CONVERSION,
                "老师，这道题我还不太懂", LocalDateTime.now().minusMinutes(10));
        createConversation(account1.getId(), ConvType.PRIVATE, s2.getId(), null, StudentStage.UNDERTAKING,
                "好的，我今晚完成作业", LocalDateTime.now().minusMinutes(45));
        createConversation(account3.getId(), ConvType.PRIVATE, s3.getId(), null, StudentStage.CONVERSION,
                "请问课程资料在哪里下载？", LocalDateTime.now().minusHours(2));
        createConversation(account4.getId(), ConvType.GROUP, null, group1.getId(), StudentStage.UNDERTAKING,
                "今晚7点准时上课", LocalDateTime.now().minusHours(1));
    }

    private TeachingGroup createGroup(String name, int sort) {
        TeachingGroup group = new TeachingGroup();
        group.setName(name);
        group.setSortOrder(sort);
        group.setStatus(1);
        return teachingGroupRepository.save(group);
    }

    private SysUser createUser(String username, String realName, int role, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(authService.encodePassword("123456"));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        return sysUserRepository.save(user);
    }

    private Tutor createTutor(Long userId, Long groupId, String employeeNo) {
        Tutor tutor = new Tutor();
        tutor.setUserId(userId);
        tutor.setTeachingGroupId(groupId);
        tutor.setEmployeeNo(employeeNo);
        tutor.setStatus(1);
        return tutorRepository.save(tutor);
    }

    private TutorWechatAccount createAccount(Long tutorId, String name, String subject, String grade,
                                             String wechatUserid, int studentCount) {
        TutorWechatAccount account = new TutorWechatAccount();
        account.setTutorId(tutorId);
        account.setAccountName(name);
        account.setSubject(subject);
        account.setGrade(grade);
        account.setWechatUserid(wechatUserid);
        account.setCorpId("corp_demo");
        account.setAgentId("agent_demo");
        account.setStudentCount(studentCount);
        account.setStatus(1);
        return accountRepository.save(account);
    }

    private Student createStudent(String nickname, String externalUserid, String grade) {
        Student student = new Student();
        student.setNickname(nickname);
        student.setExternalUserid(externalUserid);
        student.setGrade(grade);
        student.setStatus(1);
        return studentRepository.save(student);
    }

    private ChatGroup createGroupChat(Long accountId, String groupName, String chatId, int memberCount) {
        ChatGroup group = new ChatGroup();
        group.setTutorAccountId(accountId);
        group.setGroupName(groupName);
        group.setWechatChatId(chatId);
        group.setMemberCount(memberCount);
        group.setStatus(1);
        return chatGroupRepository.save(group);
    }

    private void createConversation(Long accountId, int convType, Long studentId, Long groupId,
                                    int stage, String preview, LocalDateTime lastMessageAt) {
        Conversation conversation = new Conversation();
        conversation.setTutorAccountId(accountId);
        conversation.setConvType(convType);
        conversation.setStudentId(studentId);
        conversation.setGroupId(groupId);
        conversation.setStage(stage);
        conversation.setLastMessagePreview(preview);
        conversation.setLastMessageAt(lastMessageAt);
        conversation.setStatus(1);
        conversationRepository.save(conversation);
    }
}
