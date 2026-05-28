package com.wanted.momocity.friend.enrollment;


import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "FMEnrollment")
@Table(name = "enrollment")
@NoArgsConstructor
@Getter
public class EnrollmentWithFMJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserWithFMJpaEntity userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureWithFMJpaEntity lectureId;

    @Column(name = "total_progress")
    private Long totalProgress;

    @Column(name = "completed_count")
    private Long completedCount;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

}
