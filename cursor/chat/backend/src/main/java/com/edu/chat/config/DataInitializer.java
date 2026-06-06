package com.edu.chat.config;

import com.edu.chat.model.*;
import com.edu.chat.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(
            UserRepository userRepository,
            TagRepository tagRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User agent = userRepository.save(User.builder()
                    .name("李婷")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=agent")
                    .role(UserRole.AGENT)
                    .online(true)
                    .build());

            User teacher = userRepository.save(User.builder()
                    .name("张老师")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhang")
                    .role(UserRole.TEACHER)
                    .online(true)
                    .build());

            Tag all = tagRepository.save(Tag.builder().name("全部").color("#E8E4FF").build());
            Tag math = tagRepository.save(Tag.builder().name("张老师-数学").color("#7C66FF").build());
            Tag english = tagRepository.save(Tag.builder().name("王老师-英语").color("#5B8DEF").build());
            Tag physics = tagRepository.save(Tag.builder().name("李老师-物理").color("#34C759").build());

            User liMing = userRepository.save(User.builder()
                    .name("李明")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=liming")
                    .role(UserRole.STUDENT)
                    .online(true)
                    .build());

            User wangHao = userRepository.save(User.builder()
                    .name("王浩然")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=wanghao")
                    .role(UserRole.STUDENT)
                    .online(false)
                    .build());

            User zhaoXin = userRepository.save(User.builder()
                    .name("赵欣怡")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoxin")
                    .role(UserRole.STUDENT)
                    .online(true)
                    .build());

            User chenYu = userRepository.save(User.builder()
                    .name("陈雨桐")
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=chenyu")
                    .role(UserRole.STUDENT)
                    .online(false)
                    .build());

            Conversation c1 = conversationRepository.save(Conversation.builder()
                    .type(ConversationType.PRIVATE)
                    .student(liMing)
                    .lastMessagePreview("老师，这道题我还是不太明白…")
                    .lastMessageAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                    .phase(PeriodPhase.CONVERSION)
                    .unreadCount(2)
                    .tags(Set.of(math))
                    .assignedTeacherTag(math)
                    .build());

            Conversation c2 = conversationRepository.save(Conversation.builder()
                    .type(ConversationType.PRIVATE)
                    .student(wangHao)
                    .lastMessagePreview("好的，我今晚把作业发您")
                    .lastMessageAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .phase(PeriodPhase.CONVERSION)
                    .unreadCount(0)
                    .tags(Set.of(english))
                    .assignedTeacherTag(english)
                    .build());

            Conversation c3 = conversationRepository.save(Conversation.builder()
                    .type(ConversationType.PRIVATE)
                    .student(zhaoXin)
                    .lastMessagePreview("谢谢老师！")
                    .lastMessageAt(Instant.now().minus(3, ChronoUnit.HOURS))
                    .phase(PeriodPhase.UNDERTAKING)
                    .unreadCount(1)
                    .tags(Set.of(math))
                    .assignedTeacherTag(math)
                    .build());

            conversationRepository.save(Conversation.builder()
                    .type(ConversationType.GROUP)
                    .student(chenYu)
                    .lastMessagePreview("[群聊] 下周模考安排讨论")
                    .lastMessageAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                    .phase(PeriodPhase.CONVERSION)
                    .unreadCount(0)
                    .tags(Set.of(physics))
                    .assignedTeacherTag(physics)
                    .build());

            Instant base = Instant.now().truncatedTo(ChronoUnit.MINUTES)
                    .minus(30, ChronoUnit.MINUTES);

            messageRepository.save(Message.builder()
                    .conversation(c1)
                    .sender(liMing)
                    .content("老师您好，我在做三角函数练习时遇到一道题，$f(x)=\\sin(2x+1)$ 的周期怎么求？")
                    .sentAt(base)
                    .containsMath(true)
                    .build());

            messageRepository.save(Message.builder()
                    .conversation(c1)
                    .sender(teacher)
                    .content("你好李明！正弦函数 $y=A\\sin(\\omega x+\\varphi)$ 的周期是 $T=\\frac{2\\pi}{|\\omega|}$，你这里 $\\omega=2$，所以 $T=\\pi$。")
                    .sentAt(base.plus(3, ChronoUnit.MINUTES))
                    .containsMath(true)
                    .build());

            messageRepository.save(Message.builder()
                    .conversation(c1)
                    .sender(liMing)
                    .content("明白了！那振幅和初相位呢？")
                    .sentAt(base.plus(8, ChronoUnit.MINUTES))
                    .containsMath(false)
                    .build());

            messageRepository.save(Message.builder()
                    .conversation(c1)
                    .sender(teacher)
                    .content("振幅 $|A|=1$，初相位 $\\varphi=1$（弧度）。有不清楚的随时问我。")
                    .sentAt(base.plus(12, ChronoUnit.MINUTES))
                    .containsMath(true)
                    .build());

            messageRepository.save(Message.builder()
                    .conversation(c1)
                    .sender(liMing)
                    .content("老师，这道题我还是不太明白，能再讲一遍吗？")
                    .sentAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                    .containsMath(false)
                    .build());

            messageRepository.save(Message.builder()
                    .conversation(c2)
                    .sender(wangHao)
                    .content("好的，我今晚把作业发您")
                    .sentAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .containsMath(false)
                    .build());

            // suppress unused warnings
            if (agent.getId() == null || all.getId() == null) {
                throw new IllegalStateException("seed failed");
            }
        };
    }
}
