package com.wanted.momocity.friend.infrastructure.persistence;


import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

//수강신청 후 자동 친구 처리를 위한 강의 레포지토리
public interface FriendSideLectureRepository extends JpaRepository<LectureWithFMJpaEntity, Long> {
}
