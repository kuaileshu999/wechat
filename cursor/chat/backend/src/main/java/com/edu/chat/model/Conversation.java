package com.edu.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(length = 500)
    private String lastMessagePreview;

    private Instant lastMessageAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodPhase phase;

    private int unreadCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "conversation_tags",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_teacher_tag_id")
    private Tag assignedTeacherTag;
}
