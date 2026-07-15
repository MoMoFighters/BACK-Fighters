package com.wanted.momocity.lecture.infrastructure.event;

import com.wanted.momocity.global.application.s3.S3DeletePort;
import com.wanted.momocity.lecture.domain.event.ChapterDeletedEvent;
import com.wanted.momocity.lecture.domain.event.LectureDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
// 삭제 이후 S3 파일 정리하는 이벤트 처리 헨들러
public class LectureS3DeleteEventHandler {
    private final S3DeletePort s3DeletePort;

    // 강의 삭제 트랜젝션이 정상 커밋된 후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // S3 삭제가 API 응답 시간을 지연시키지 않도록 별도 스레드에서 실행
    @Async("domainEventExecutor")
    public void handleLectureDeleted(LectureDeletedEvent event) {
        //삭제된 강의의 모든 S3 파일을 포함하는 prefix 실행
        String prefix = "lectures/" + event.lectureId() + "/";

        // 생성한 강의 prefix에 포함된 S3 객체 삭제
        deletePrefixSafely(
                "강의",
                prefix
        );
    }

    // 챕터 삭제 트랜잭션이 정상 커밋된 후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // 영상 미 썸네일 삭제를 별도 스레드에서 실행
    @Async("domainEventExecutor")
    public void handleChapterDeleted(ChapterDeletedEvent event) {
        String prefix = "lectures/" + event.lectureId() + "/chapters/" + event.chapterId() + "/";

        deletePrefixSafely(
                "챕터",
                prefix
        );
    }

    // S3 오류가 발생해도 이미 완료된 DB 삭제 흐름에 영향을 주지 않도록 처리
    private void deletePrefixSafely(String target, String prefix) {
        try {
            s3DeletePort.deleteByPrefix(prefix);

            log.info("[Lecture] {} S3 파일 삭제 완료 - prefix={}",
                    target,
                    prefix);
        } catch (Exception exception) {
            log.error("[Lecture] {} S3 파일 삭제 실패 - prefix={}",
                    target,
                    prefix,
                    exception
            );
        }
    }
}
