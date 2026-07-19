package com.wanted.momocity.study.infrastructure.scheduler;

import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.domain.repository.StudyLapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  ENDED된 지 30일 지난 group_room을 하드딜리트하는 배치 스케줄러
 *  -
 *  삭제 순서 (FK 유무에 따라 다름):
 *  1) study_lap: group_room과 FK 관계가 없어서 직접 삭제 필요
 *  2) group_room: 삭제하면 group_room_member는 fk_group_room_member_room(ON DELETE CASCADE)로 자동 삭제됨
 *  -
 *  매일 새벽 4시 1회 실행
 * */

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyCleanupScheduler {

    private static final int RETENTION_DAYS = 30;

    private final GroupRoomRepository groupRoomRepository;
    private final StudyLapRepository studyLapRepository;

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupEndedRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<GroupRoom> targets = groupRoomRepository
                .findAllByStatusAndDeletedAtBefore(GroupRoom.GroupRoomStatus.ENDED, threshold);

        if (targets.isEmpty()) {
            log.info("[StudyCleanup] 하드딜리트 대상 없음");
            return;
        }
        log.info("[StudyCleanup] 하드딜리트 대상 {}건 발견", targets.size());

        for (GroupRoom room : targets) {
            try {
                deleteRoom(room.getId());
                log.info("[StudyCleanup] 방 하드딜리트 완료 | roomId={}, endedAt={}", room.getId(), room.getDeletedAt());
            } catch (Exception e) {
                // 방 하나 삭제 실패가 전체 배치를 중단시키지 않도록 개별 try-catch로 격리
                log.error("[StudyCleanup] 방 하드딜리트 실패 | roomId={}, message={}", room.getId(), e.getMessage(), e);
            }
        }
    }

    // 방 하나를 삭제 - study_lap 먼저 지우고 group_room 삭제 (member는 CASCADE)
    // 각 방 삭제를 별도 트랜잭션으로 묶어서, 하나 실패해도 다른 방 삭제에 영향 없게 함
    @Transactional
    public void deleteRoom(Long roomId) {
        studyLapRepository.deleteAllByRoomId(roomId);
        groupRoomRepository.deleteById(roomId);
    }

}
