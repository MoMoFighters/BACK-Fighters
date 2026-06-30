package com.wanted.momocity.global.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MomoMetrics {

    private final MeterRegistry meterRegistry;

    // ===== Timer =====
    private final Timer s3UploadTimer;
    private final Timer blacklistCheckTimer;
    private final Timer enrollmentTimer;
    private final Timer friendListTimer;
    private final Timer messageHistoryTimer;
    private final Timer chatRoomListTimer;
    private final Timer lectureUploadTimer;
    private final Timer lectureListTimer;
    private final Timer reviewCreateTimer;
    private final Timer reviewDeleteTimer;
    private final Timer reviewListTimer;
    private final Timer enrollmentProgressTimer;
    private final Timer chapterCreateTimer;
    private final Timer chapterVideoRegisterTimer;
    private final Timer adminUserListTimer;
    private final Timer teacherApproveTimer;

    // ===== Counter =====
    private final Counter s3UploadFailCounter;
    private final Counter reviewCreatedCounter;
    private final Counter reviewDeletedCounter;
    private final Counter duplicateReviewFailedCounter;


    public MomoMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: S3 업로드 소요 시간
        this.s3UploadTimer = Timer.builder("momocity.s3.upload.duration")
                .description("S3 파일 업로드 소요 시간")
                .register(meterRegistry);

        // Timer: 블랙리스트 Redis 조회 소요 시간
        // 모든 API 요청마다 실행 — S3 presigned URL 발급 시 135ms 소요 확인됨
        this.blacklistCheckTimer = Timer.builder("momocity.blacklist.check.duration")
                .description("요청마다 실행되는 블랙리스트 Redis 조회 소요 시간")
                .register(meterRegistry);


        // Timer: 수강 신청 소요 시간
        this.enrollmentTimer = Timer.builder("momocity.enrollment.duration")
                .description("수강 신청 소요 시간")
                .register(meterRegistry);

        // Timer: 친구 목록 조회 소요 시간
        // 93ms, SQL 10개, N+1 발생 확인됨 — 최적화 전후 비교용
        this.friendListTimer = Timer.builder("momocity.friend.list.duration")
                .description("친구 목록 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);

        // Timer: 메시지 내역 조회 소요 시간
        // 171ms, SQL 14개, N+1 발생 확인됨 — 최적화 before/after 비교용
        this.messageHistoryTimer = Timer.builder("momocity.message.history.duration")
                .description("메시지 내역 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);

        // Timer: 채팅방 목록 조회 소요 시간
        // 137ms, SQL 14개 확인됨
        this.chatRoomListTimer = Timer.builder("momocity.chatroom.list.duration")
                .description("채팅방 목록 조회 소요 시간")
                .register(meterRegistry);

        // Timer: 강의 등록 소요 시간
        // 트래픽 몰림, 영상 길이에 따른 S3 업로드 포함 전체 시간 측정
        this.lectureUploadTimer = Timer.builder("momocity.lecture.upload.duration")
                .description("강의 등록 소요 시간 - S3 업로드 포함")
                .register(meterRegistry);

        // Timer: 강의 목록 조회 소요 시간
        this.lectureListTimer = Timer.builder("momocity.lecture.list.duration")
                .description("강의 목록 조회 소요 시간")
                .register(meterRegistry);

        // Counter: S3 업로드 실패 횟수
        this.s3UploadFailCounter = Counter.builder("momocity.s3.upload.failed")
                .description("S3 파일 업로드 실패 횟수")
                .register(meterRegistry);


        this.reviewCreateTimer = Timer.builder("momocity.review.create.duration")
                .description("수강평 등록 소요 시간")
                .register(meterRegistry);

        this.reviewDeleteTimer = Timer.builder("momocity.review.delete.duration")
                .description("수강평 삭제 소요 시간")
                .register(meterRegistry);

        this.reviewListTimer = Timer.builder("momocity.review.list.duration")
                .description("수강평 목록 조회 소요 시간")
                .register(meterRegistry);

        this.reviewCreatedCounter = Counter.builder("momocity.review.created")
                .description("수강평 등록 성공 횟수")
                .register(meterRegistry);

        this.reviewDeletedCounter = Counter.builder("momocity.review.deleted")
                .description("수강평 삭제 성공 횟수")
                .register(meterRegistry);

        this.duplicateReviewFailedCounter = Counter.builder("momocity.review.duplicate.failed")
                .description("중복 수강평 등록 실패 횟수")
                .register(meterRegistry);

        this.enrollmentProgressTimer = Timer.builder("momocity.enrollment.progress.duration")
                .description("학습 진척도 조회 소요 시간")
                .register(meterRegistry);

        this.chapterCreateTimer = Timer.builder("momocity.chapter.create.duration")
                .description("챕터 등록 소요 시간")
                .register(meterRegistry);

        this.chapterVideoRegisterTimer = Timer.builder("momocity.chapter.video.register.duration")
                .description("챕터 동영상 등록 소요 시간")
                .register(meterRegistry);

        this.adminUserListTimer = Timer.builder("momocity.admin.user.list.duration")
                .description("관리자 회원 목록 조회 소요 시간 - Redis 캐싱 전후 비교")
                .register(meterRegistry);

        this.teacherApproveTimer = Timer.builder("momocity.teacher.approve.duration")
                .description("강사 승인 처리 소요 시간 - bulk UPDATE 최적화 전후 비교")
                .register(meterRegistry);
    }

    // 작업 시작 시점의 시간을 기억
    // 실제 Timer는 작업이 끝난 뒤 sample.stop(timer)를 호출할 때 결정된다.
    // try-finally로 감싸서 성공/실패 여부와 관계없이 시간을 기록할 수 있다.
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // S3 업로드 전체 소요 시간을 기록
    public void stopS3UploadTimer(Timer.Sample sample) {
        sample.stop(s3UploadTimer);
    }

    // S3 업로드 실패 횟수를 기록
    public void recordS3UploadFailed() {
        s3UploadFailCounter.increment();
    }

    // 블랙리스트 Redis 조회 소요 시간을 기록
    // 모든 API 요청마다 실행되므로 누적 지연이 전체 응답시간에 미치는 영향을 파악 가능
    public void stopBlacklistCheckTimer(Timer.Sample sample) {
        sample.stop(blacklistCheckTimer);
    }

    // 수강 신청 소요 시간 기록
    public void stopEnrollmentTimer(Timer.Sample sample) {
        sample.stop(enrollmentTimer);
    }

    // 친구 목록 조회 소요 시간 기록
    public void stopFriendListTimer(Timer.Sample sample) {
        sample.stop(friendListTimer);
    }

    // 메시지 내역 조회 소요 시간 기록
    public void stopMessageHistoryTimer(Timer.Sample sample) {
        sample.stop(messageHistoryTimer);
    }

    // 채팅방 목록 조회 소요 시간 기록
    public void stopChatRoomListTimer(Timer.Sample sample) {
        sample.stop(chatRoomListTimer);
    }

    // 강의 등록 소요 시간 기록
    public void stopLectureUploadTimer(Timer.Sample sample) {
        sample.stop(lectureUploadTimer);
    }

    // 강의 목록 조회 소요 시간 기록
    public void stopLectureListTimer(Timer.Sample sample) {
        sample.stop(lectureListTimer);
    }

    // 수강평 등록 소요 시간 기록
    public void stopReviewCreateTimer(Timer.Sample sample) {
        sample.stop(reviewCreateTimer);
    }

    // 수강평 삭제 소요 시간 기록
    public void stopReviewDeleteTimer(Timer.Sample sample) {
        sample.stop(reviewDeleteTimer);
    }

    public void stopReviewListTimer(Timer.Sample sample) {
        sample.stop(reviewListTimer);
    }

    public void recordReviewCreated() {
        reviewCreatedCounter.increment();
    }

    public void recordReviewDeleted() {
        reviewDeletedCounter.increment();
    }

    public void recordDuplicateReviewFailed() {
        duplicateReviewFailedCounter.increment();
    }

    public void stopEnrollmentProgressTimer(Timer.Sample sample) {
        sample.stop(enrollmentProgressTimer);
    }

    public void stopChapterCreateTimer(Timer.Sample sample) {
        sample.stop(chapterCreateTimer);
    }

    public void stopChapterVideoRegisterTimer(Timer.Sample sample) {
        sample.stop(chapterVideoRegisterTimer);
    }

    public void stopAdminUserListTimer(Timer.Sample sample) { sample.stop(adminUserListTimer); }

    public void stopTeacherApproveTimer(Timer.Sample sample) { sample.stop(teacherApproveTimer); }

}