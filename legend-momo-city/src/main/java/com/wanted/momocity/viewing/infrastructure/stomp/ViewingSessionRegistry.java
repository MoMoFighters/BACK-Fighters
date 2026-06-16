package com.wanted.momocity.viewing.infrastructure.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/*
* comment.
*  [역할]
*  STOMP 세션 별 마지막 재생 상태를 메모리에 저장
*  -> 연결 끊김 시 lastPositionSec 저장에 사용
*  -
*  [저장 구조]
*  sessionId -> ViewingSessionInfo (userId, lectureId, chapterId, lastPlaybackSeconds)
*  -
*  [ConcurrentHashMap 사용 이유]
*  - 다중 사용자가 동시에 접근 -> thread-safe 자료구조 필요
*  -> HashMap 은 thread-safe 하지 않아 동시성 문제 발생 가능
* */

@Slf4j
@Component
public class ViewingSessionRegistry {

    // ConcurrentHashMap
    // key : STOMP 세션 ID (연결마다 고유)
    // value : 세션 정보 (userId, lectureId, chapterId, lastPlaybackSeconds)
    private final ConcurrentHashMap<String, ViewingSessionInfo> sessionMap
            = new ConcurrentHashMap<>();

    // saveOrUpdate : STOMP 메세지 수신 시마다 호출 -> 세션 정보 저장 또는 업데이트
    public void saveOrUpdate(String sessionId, Long userId, Long lectureId,
                             Long chapterId, int playbackSeconds) {
        sessionMap.put(sessionId, new ViewingSessionInfo(
                userId, lectureId, chapterId, playbackSeconds
        ));
        log.debug("[Viewing] 세션 정보 업데이트 | sessionId={}, userId={}, chapterId={}, playbackSeconds={}",
                sessionId, userId, chapterId, playbackSeconds);
    }

    // getAndRemove : 연결 끊김 시 호출 -> 세션 정보 꺼내고 삭제
    public ViewingSessionInfo getAndRemove(String sessionId) {
        ViewingSessionInfo info = sessionMap.remove(sessionId);
        log.debug("[Viewing] 세션 정보 제거 | sessionId={}, info={}",
                sessionId, info);
        return info;
    }

    // ViewingSessionInfo : 세션별 저장 데이터 -> Record 로 불변 객체 설계
    public record ViewingSessionInfo(
            Long userId,
            Long lectureId,
            Long chapterId,
            int lastPlaybackSeconds
    ) {}
}
