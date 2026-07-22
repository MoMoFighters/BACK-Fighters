package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataChatRoomMemberRepository extends JpaRepository<ChatRoomMemberJpaEntity, Long> {
    //개선
    //로그인 유저가 들어있는 모든 채팅방 멤버 행 조회
    // [패치 조인 적용] 로그인 유저의 멤버 행들을 조회할 때, 연관된 채팅방(roomId) 정보까지 한방에 긁어옵니다.
    @Query("select cm from ChatRoomMemberJpaEntity cm " +
            "join fetch cm.roomId " +
            "join fetch cm.userId " +
            "where cm.userId.id = :userId")
    List<ChatRoomMemberJpaEntity> findByUserId_Id(@Param("userId") Long userId);

    //특정 방에 속한 모든 멤버들 조회(상대방을 고르기 위함)
    // [패치 조인 적용] 특정 방 ID로 멤버들을 긁어올 때,
    // 연관된 유저(userId) 정보까지 한방에 패치 조인으로 묶어서 가져옵니다.
    @Query("select cm from ChatRoomMemberJpaEntity cm " +
            "join fetch cm.userId " +
            "where cm.roomId.id = :roomId")
    List<ChatRoomMemberJpaEntity> findByRoomId_Id(@Param("roomId") Long roomId);

    //회원가입 완료 이벤트로 나와의 채팅 생성 시 나와의 채팅방 기존 존재 여부 확인
    boolean existsByUserId_Id(Long userId);

    //나와의 채팅방 찾기 위함
    @Query("select min(m.roomId.id) from ChatRoomMemberJpaEntity m where m.userId.id = :userId")
    Optional<Long> findFirstRoomIdByUserId(@Param("userId") Long userId);

    //메시지 전송(로그인한 유저가 해당 채팅방의 멤버인지 확인)
    boolean existsByRoomId_IdAndUserId_Id(Long roomId, Long userId);

    //채팅방 조회 및 개설: 일대일 채팅방 연계를 위함(두 유저가 포함되고 방이름 없는 일대일 채팅방 정보 가져오기)
    List<ChatRoomMemberJpaEntity> findByUserId_IdInAndRoomId_RoomTitleIsNull(List<Long> userId);

    //친구 삭제 후 채팅방 나가기 버그 수정
    @Query("select m1.roomId.id " +
            "from ChatRoomMemberJpaEntity m1 " +
            "join ChatRoomMemberJpaEntity m2 on m1.roomId.id = m2.roomId.id " +
            "where m1.userId.id = :userId " +
            "  and m2.userId.id = :targetUserId " +
            "  and (m1.roomId.roomTitle is null or m1.roomId.roomTitle = '')")
    Optional<Long> findOneToOneChatRoomIdBetween(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
    //친구 삭제 후 채팅방 나가기 버그 수정
    @Query("select m from ChatRoomMemberJpaEntity m where m.roomId.id = :roomId and m.userId.id = :userId")
    Optional<ChatRoomMemberJpaEntity> findMemberByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    //채팅방 목록 조회 개선 보강
    // SpringDataChatRoomMemberRepository
    @Query("select cm from ChatRoomMemberJpaEntity cm join fetch cm.userId where cm.roomId.id in :roomIds")
    List<ChatRoomMemberJpaEntity> findByRoomId_IdIn(@Param("roomIds") List<Long> roomIds);

    //채팅방 조회 및 개설 개선 보강
    @Query("select m from ChatRoomMemberJpaEntity m " +
            "join fetch m.roomId r " +
            "where m.userId.id = :targetUserId " +
            "and r.roomTitle is null " +
            "and (select count(sub) from ChatRoomMemberJpaEntity sub where sub.roomId = r) = 1 " +
            "and r.id != (select min(subM.roomId.id) from ChatRoomMemberJpaEntity subM where subM.userId.id = :loginUserId)")
    List<ChatRoomMemberJpaEntity> findOnePersonRoomsByUserId(
            @Param("targetUserId") Long targetUserId,
            @Param("loginUserId") Long loginUserId
    );
}
