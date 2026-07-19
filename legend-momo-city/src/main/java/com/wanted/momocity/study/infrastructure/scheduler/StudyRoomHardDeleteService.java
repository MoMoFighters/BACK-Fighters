package com.wanted.momocity.study.infrastructure.scheduler;

import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.domain.repository.StudyLapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  방 하나를 하드딜리트하는 트랜잭션 단위를 스케줄러에서 분리한 빈
 *  -
 *  같은 클래스 안에서 this.deleteRoom()으로 자기 자신을 호출하면
 *  Spring AOP 프록시를 거치지 않아 @Transactional이 적용되지 않는 문제(self-invocation) 존재
 *  별도 빈으로 분리해 스케줄러가 프록시를 통해 외부에서 호출하도록 함
 * */

@Component
@RequiredArgsConstructor
public class StudyRoomHardDeleteService {

    private final GroupRoomRepository groupRoomRepository;
    private final StudyLapRepository studyLapRepository;

    // study_lap 먼저 지우고 group_room 삭제 (member는 FK CASCADE로 자동 삭제)
    // 방 하나 단위로 트랜잭션을 묶어, 방별 실패가 서로 영향 없게 함
    @Transactional
    public void deleteRoom(Long roomId) {
        studyLapRepository.deleteAllByRoomId(roomId);
        groupRoomRepository.deleteById(roomId);
    }

}
