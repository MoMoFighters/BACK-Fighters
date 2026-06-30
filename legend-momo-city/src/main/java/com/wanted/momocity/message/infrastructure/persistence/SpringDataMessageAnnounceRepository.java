package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataMessageAnnounceRepository extends JpaRepository<MessageAnnounceJpaEntity, Long> {


    //채팅방 목록 정렬을 위한 안내 문구 시간 확인
    // 🎯 [패치 조인 적용] 방 ID로 가장 최신의 안내 문구를 조회할 때,
    // 안내 문구의 대상이 되는 유저 정보(targetId)까지 한방에 패치 조인으로 묶어서 가져옵니다.
    @Query("select ma from MessageAnnounceJpaEntity ma " +
            "join fetch ma.targetId " +
            "where ma.roomId.id = :roomId " +
            "order by ma.createdAt desc limit 1")
    Optional<MessageAnnounceJpaEntity> findFirstByRoomId_IdOrderByCreatedAtDesc(@Param("roomId") Long roomId);

    //채팅방 나가기: 안내 문구 삭제
    @Modifying(clearAutomatically = true)
    @Query("delete from MessageAnnounceJpaEntity ma where ma.roomId.id = :roomId")
    void deleteByRoomId_Id(@Param("roomId") Long roomId);

    //안내 문구 내역 조회(재입장 고려)
    @Query("select ma from MessageAnnounceJpaEntity ma " +
            "left join fetch ma.targetId " +
            "where ma.roomId.id = :roomId " +
            "  and ma.createdAt between :startTimeLine and :endTimeLine")
    List<MessageAnnounceJpaEntity> findByRoomId_IdAndCreatedAtBetween(@Param("roomId") Long roomId,
                                                                      @Param("startTimeLine") LocalDateTime startTimeLine,
                                                                      @Param("endTimeLine") LocalDateTime endTimeLine);

    //재입장 시 안내 문구 내역도 조회
    boolean existsAnnounceByRoomId_IdAndTargetId_Id(Long roomId, Long targetId);

    //채팅방 목록 조회 개선 보강
    // SpringDataMessageAnnounceRepository (방별 최신 안내문구 시간 그룹화 조회)
    @Query("select ma.roomId.id, max(ma.createdAt) from MessageAnnounceJpaEntity ma where ma.roomId.id in :roomIds group by ma.roomId.id")
    List<Object[]> findLatestAnnounceTimeByRoomIdsIn(@Param("roomIds") List<Long> roomIds);
}
