package com.wanted.momocity.study.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/*
 * comment.
 *  그룹방 인원수를 Redis에서 원자적으로 관리하는 어댑터
 *  -
 *  [Redisson RLock으로 교체한 이유]
 *  Lua 스크립트(GET+INCR 원자적 처리)로도 이미 검증된 안전한 방식이었으나,
 *  향후 여러 Redis 키를 넘나드는 복잡한 로직이나 MySQL 트랜잭션과 섞인 검증이 필요해질 가능성을 고려해
 *  "진짜 분산 락" 패턴(임계구역을 락으로 감싸는 방식)으로 전환
 *  -
 *  Lua는 "GET+INCR" 딱 그 안에서만 원자성을 보장하는 반면,
 *  RLock은 락을 잡은 뒤 그 안에서 여러 작업(DB 조회, 다른 Redis 키 조작 등)을 자유롭게 수행 가능
 *  -
 *  [역할 분담]
 *  발송 시점(선제 차단)은 MemberCommandService가 DB(JOINED+INVITED 개수)로 대략 확인,
 *  이 어댑터는 "수락 시점 최종 방어선" 역할만 담당
 *  -
 *  [키 구조]
 *  카운트 키: study:room:{roomId}:member-count
 *  락 키: study:room:{roomId}:lock (카운트 키와 별개의 락 전용 키)
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
    private static final String COUNT_KEY_SUFFIX = ":member-count";
    private static final String LOCK_KEY_SUFFIX = ":lock";

    // 락 대기 시간 - 정원 체크 로직 자체가 GET+INCR 두 줄이라 짧게 설정
    private static final long WAIT_TIME_SECONDS = 2L;

    // 단순 문자열(숫자) 값 하나만 다루면 되므로 StringRedisTemplate 사용 (직렬화 오버헤드 없음)
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    /*
     * comment.
     *  tryIncrement()의 결과를 세분화한 열거형
     *  - ROOM_FULL: 실제로 정원이 가득 참 (정확한 사용자 메시지 필요)
     *  - LOCK_ACQUISITION_FAILED: 락 획득 자체에 실패 (일시적 경합, 정원 초과와 다른 의미)
     *  기존에는 둘 다 boolean false로 뭉뚱그려서 "정원이 가득 찼습니다"로 나갔는데,
     *  실제로는 원인이 다르므로 호출부에서 구분해서 처리할 수 있도록 분리
     * */
    public enum IncrementResult {
        SUCCESS,
        ROOM_FULL,
        LOCK_ACQUISITION_FAILED
    }

    /*
     * comment.
     *  인원 카운트 초기화 (방 생성 시 호출)
     *  - room 도메인(RoomCommandService.createRoom)에서 방과 방장 멤버를 저장한 직후 호출
     *  - initialCount는 보통 1(방장 본인)로 시작
     *  - 기존 키가 있어도 덮어쓴다(set) - 같은 roomId가 재사용될 일은 없으므로 안전
     * */
    public void initialize(Long roomId, long initialCount) {
        redisTemplate.opsForValue().set(countKey(roomId), String.valueOf(initialCount));
    }

    /*
     * comment.
     *  원자적으로 인원 +1을 시도한다. (초대 수락 시 최종 방어선)
     *  -
     *  Lua 스크립트 내부 동작 :
     *  1. 현재 카운트를 GET으로 확인
     *  2. maxMember 미만이면 INCR 실행 후 증가된 값 반환
     *  3. maxMember 이상이면 INCR 자체를 실행하지 않고 -1 반환
     *  -
     *  이 GET+INCR 두 단계는 Redis 싱글 스레드 특성상 Lua 스크립트 실행 중에는
     *  다른 명령이 절대 끼어들 수 없으므로, 기존 방식에 있던 "일시적 초과" 순간이 사라짐
     * */

    public IncrementResult tryIncrement(Long roomId, int maxMember) {
        RLock lock = redissonClient.getLock(lockKey(roomId));
        boolean acquired = false;

        try {
            acquired = lock.tryLock(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                // 락 획득 자체에 실패 - 정원 초과와는 다른 의미의 실패지만 호출부에서는 동일하게 처리
                return IncrementResult.LOCK_ACQUISITION_FAILED;
            }

            String current = redisTemplate.opsForValue().get(countKey(roomId));
            int currentCount = (current != null) ? Integer.parseInt(current) : 0;

            if (currentCount >= maxMember) {
                return IncrementResult.ROOM_FULL;
            }

            redisTemplate.opsForValue().increment(countKey(roomId));
            return IncrementResult.SUCCESS;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return IncrementResult.LOCK_ACQUISITION_FAILED;
        } finally {
            // 락을 실제로 획득했을 때만 해제 (획득 못 했는데 unlock 시도하면 예외 발생)
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /*
     * comment.
     *  인원 -1 (퇴장, 강퇴 등으로 참가자가 실제로 줄어들 때 호출)
     *  - MemberCommandService.leave(), kick()에서 각각 호출
     * */
    public void decrement(Long roomId) {
        redisTemplate.opsForValue().decrement(countKey(roomId));
    }

    /*
     * comment.
     *  방 종료 시 카운트 키 자체를 삭제
     *  - decrement()로 0까지 내리는 대신 clear()로 아예 지우는 이유 :
     *    방이 소프트딜리트된 이후에는 이 방에 대한 카운트 자체가 더 이상 의미가 없고,
     *    키를 계속 남겨두면 Redis 메모리만 낭비하게 되므로 명시적으로 정리
     * */
    public void clear(Long roomId) {
        redisTemplate.delete(countKey(roomId));
    }

    private String countKey(Long roomId) {
        return KEY_PREFIX + roomId + COUNT_KEY_SUFFIX;
    }

    private String lockKey(Long roomId) {
        return KEY_PREFIX + roomId + LOCK_KEY_SUFFIX;
    }
}