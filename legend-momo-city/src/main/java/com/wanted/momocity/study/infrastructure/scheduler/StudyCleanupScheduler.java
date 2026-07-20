package com.wanted.momocity.study.infrastructure.scheduler;

import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final StudyRoomHardDeleteService studyRoomHardDeleteService;

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
                // this.deleteRoom() 자기 자신 호출 -> 프록시를 거치는 외부 빈 호출로 변경
                // (같은 클래스 내부 호출은 @Transactional 프록시를 우회해 트랜잭션이 안 걸리는 문제가 있었음)
                studyRoomHardDeleteService.deleteRoom(room.getId());
                log.info("[StudyCleanup] 방 하드딜리트 완료 | roomId={}, endedAt={}", room.getId(), room.getDeletedAt());
            } catch (Exception e) {
                // 방 하나 삭제 실패가 전체 배치를 중단시키지 않도록 개별 try-catch로 격리
                log.error("[StudyCleanup] 방 하드딜리트 실패 | roomId={}, message={}", room.getId(), e.getMessage(), e);
            }
        }
    }

}
