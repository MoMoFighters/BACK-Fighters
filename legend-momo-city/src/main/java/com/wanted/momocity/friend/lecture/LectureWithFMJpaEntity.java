package com.wanted.momocity.friend.lecture;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "FMLecture")
@Table(name = "lecture")
@NoArgsConstructor
@Getter
public class LectureWithFMJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private UserWithFMJpaEntity teacherId;

    @Column(name = "title")
    private String title;
}
