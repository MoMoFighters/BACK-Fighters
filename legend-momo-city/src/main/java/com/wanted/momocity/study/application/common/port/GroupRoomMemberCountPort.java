package com.wanted.momocity.study.application.common.port;

/*
 * comment.
 *  그룹방 인원수를 원자적으로 관리하는 기능에 대한 포트
 *  - study 컨텍스트 전체가 공용으로 쓰는 Port (member의 수락 로직, room의 생성 로직 양쪽에서 사용)
 *  - 구현체 : infrastructure.redis.GroupRoomMemberCountAdapter
 * */


public interface GroupRoomMemberCountPort {

    // 인원 카운트 초기화 (방 생성 시 - 방장 포함 initialCount로 시작)
    void initialize(Long roomId, long initialCount);

    // 원자적으로 인원 +1 시도 - 정원(maxMember) 초과 시 false 반환 (수락 시점 최종 방어선)
    boolean tryIncrement(Long roomId, int maxMember);

    // 인원 -1 (퇴장, 강퇴 등으로 참가자가 줄어들 때)
    void decrement(Long roomId);

    // 방 종료 시 카운트 자체를 정리
    void clear(Long roomId);

}
