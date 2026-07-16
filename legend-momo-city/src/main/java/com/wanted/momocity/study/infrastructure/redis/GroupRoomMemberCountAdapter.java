package com.wanted.momocity.study.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  그룹방 인원수를 Redis에서 원자적으로 관리하는 어댑터
 *  -
 *  방 정원(4명) 체크를 DB 카운트(SELECT COUNT)로만 하면, 여러 명이 동시에 마지막 자리를
 *  두고 수락 요청을 보낼 때 "조회 -> 판단 -> 저장" 사이에 다른 요청이 끼어들어
 *  정원을 초과해서 저장되는 레이스 컨디션이 존재 가능
 *  Redis의 INCR 연산은 "읽고 -> 더하고 -> 저장"이 하나의 원자적 명령으로 처리되므로,
 *  동시에 여러 요청이 와도 순서가 보장되어 절대 초과 불가능
 *  -
 *  [역할 분담]
 *  발송 시점(선제 차단)은 MemberCommandService가 DB(JOINED+INVITED 개수)로 대략 확인,
 *  이 어댑터는 "수락 시점 최종 방어선" 역할만 담당
 *  (발송 시점까지 Redis로 엄격하게 처리할 필요는 없음 - 어차피 최종 확정은 수락 시점이므로)
 *  -
 *  [키 구조]
 *  study:room:{roomId}:member-count
 *  - 방 생성 시(RoomCommandService.createRoom) 방장 포함 1로 초기화
 *  - 수락 성공 시 tryIncrement()로 +1
 *  - 퇴장/강퇴 시(MemberCommandService.leave/kick) decrement()로 -1
 *  - 방 종료 시(인원 0명) clear()로 키 자체 삭제
 *  -
 *  [TTL을 두지 않는 이유]
 *  방이 살아있는 동안에는 언제든 정확한 카운트가 필요하므로 자동 만료시키면 안됨
 *  방이 소프트딜리트될 때 명시적으로 clear()를 호출해서 지움
 * */

@Component
@RequiredArgsConstructor
public class GroupRoomMemberCountAdapter {

    // Redis 키 접두사/접미사 - 다른 도메인 키와 충돌하지 않도록 study:room: 으로 네임스페이스 구분
    private static final String KEY_PREFIX = "study:room:";
    private static final String KEY_SUFFIX = ":member-count";

    // 단순 문자열(숫자) 값 하나만 다루면 되므로 StringRedisTemplate 사용 (직렬화 오버헤드 없음)
    private final StringRedisTemplate redisTemplate;

    /*
     * comment.
     *  인원 카운트 초기화 (방 생성 시 호출)
     *  - room 도메인(RoomCommandService.createRoom)에서 방과 방장 멤버를 저장한 직후 호출
     *  - initialCount는 보통 1(방장 본인)로 시작
     *  - 기존 키가 있어도 덮어쓴다(set) - 같은 roomId가 재사용될 일은 없으므로 안전
     * */
    public void initialize(Long roomId, long initialCount) {
        redisTemplate.opsForValue().set(key(roomId), String.valueOf(initialCount));
    }

    /*
     * comment.
     *  원자적으로 인원 +1을 시도한다. (초대 수락 시 최종 방어선)
     *  -
     *  동작 순서 :
     *  1. INCR로 먼저 +1 실행 (일단 자리를 선점)
     *  2. 그 결과값이 maxMember를 넘으면, 자리가 없었다는 뜻이므로 즉시 DECR로 되돌리고 false 반환
     *  3. 넘지 않으면 정상적으로 자리를 확보한 것이므로 true 반환
     *  -
     *  INCR 후 초과분을 되돌리는 방식이라, 아주 짧은 순간 "일시적으로 초과된 값"이 Redis에 존재 가능
     *  다만 그 순간에 그 값을 읽어서 판단에 쓰는 다른 로직이 없으므로 실질적인 문제는 없다고 판단
     * */

    public boolean tryIncrement(Long roomId, int maxMember) {
        // increment()는 Redis INCR 명령을 그대로 호출 - 키가 없으면 0에서 시작해서 1을 반환
        Long current = redisTemplate.opsForValue().increment(key(roomId));

        if (current == null) {
            // Redis 연결 자체에 문제가 있는 극히 예외적인 상황 - 안전하게 실패 처리
            return false;
        }

        if (current > maxMember) {
            // 정원을 넘겼으므로 방금 올린 카운트를 즉시 되돌린다 (자리를 선점했다가 취소)
            redisTemplate.opsForValue().decrement(key(roomId));
            return false;
        }

        // 정원 이내이므로 자리 확보 성공
        return true;
    }

    /*
     * comment.
     *  인원 -1 (퇴장, 강퇴 등으로 참가자가 실제로 줄어들 때 호출)
     *  - MemberCommandService.leave(), kick()에서 각각 호출
     * */
    public void decrement(Long roomId) {
        redisTemplate.opsForValue().decrement(key(roomId));
    }

    /*
     * comment.
     *  방 종료 시 카운트 키 자체를 삭제
     *  - decrement()로 0까지 내리는 대신 clear()로 아예 지우는 이유 :
     *    방이 소프트딜리트된 이후에는 이 방에 대한 카운트 자체가 더 이상 의미가 없고,
     *    키를 계속 남겨두면 Redis 메모리만 낭비하게 되므로 명시적으로 정리
     * */
    public void clear(Long roomId) {
        redisTemplate.delete(key(roomId));
    }

    // Redis 키 조합 - study:room:{roomId}:member-count 형태로 생성
    private String key(Long roomId) {
        return KEY_PREFIX + roomId + KEY_SUFFIX;
    }
}