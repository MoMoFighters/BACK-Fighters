package com.wanted.momocity.global.infrastructure.metrics;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAop {

    private final MomoMetrics momoMetrics;

    // S3 업로드 소요 시간 측정
    // 업로드 실패 횟수도 함께 기록
    @Around("execution(* com.wanted.momocity.global.application.s3.S3UploadPort.upload(..))")
    public Object measureS3Upload(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            momoMetrics.recordS3UploadFailed();
            throw e;
        } finally {
            momoMetrics.stopS3UploadTimer(sample);
        }
    }

    // 블랙리스트 Redis 조회 소요 시간 측정
    // 모든 API 요청마다 실행되므로 누적 지연 파악 가능
    @Around("execution(* com.wanted.momocity.auth.application.port.BlacklistPort.isBlacklisted(..))")
    public Object measureBlacklistCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopBlacklistCheckTimer(sample);
        }
    }

    // 메시지 내역 조회 소요 시간 측정
    // 기존 : 171ms, SQL 14개, N+1 확인
    @Around("execution(* com.wanted.momocity.message.application.service.MessageQueryService.getMessageHistoryQueryHandle(..))")
    public Object measureMessageHistory(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopMessageHistoryTimer(sample);
        }
    }

    // 채팅방 목록 조회 소요 시간 측정
    // 기존 : 137ms, SQL 14개
    @Around("execution(* com.wanted.momocity.message.application.service.MessageQueryService.getChatRoomQueryHandle(..))")
    public Object measureChatRoomList(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopChatRoomListTimer(sample);
        }
    }

    // 친구 목록 조회 소요 시간 측정
    // 기존 : 93ms, SQL 10개, N+1 확인
    @Around("execution(* com.wanted.momocity.friend.application.service.FriendQueryService.getFriendQueryHandle(..))")
    public Object measureFriendList(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopFriendListTimer(sample);
        }
    }

    // 강의 등록 소요 시간 측정
    @Around("execution(* com.wanted.momocity.lecture.application.service.LectureCommandService.createLecture(..))")
    public Object measureLectureUpload(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopLectureUploadTimer(sample);
        }
    }

    // 강의 목록 조회 소요 시간 측정
    @Around("execution(* com.wanted.momocity.lecture.application.service.LectureQueryService.getLectures(..))")
    public Object measureLectureList(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopLectureListTimer(sample);
        }
    }

    // 수강 신청 소요 시간 측정
    @Around("execution(* com.wanted.momocity.enrollment.application.service.EnrollmentCommandService.createEnrollment(..))")
    public Object measureEnrollment(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopEnrollmentTimer(sample);
        }
    }

    // 수강평 등록 소요 시간 측정
    @Around("execution(* com.wanted.momocity.review.application.service.ReviewCommandService.createReview(..))")
    public Object measureReviewCreate(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopReviewCreateTimer(sample);
        }
    }

    // 수강평 삭제 소요 시간 측정
    @Around("execution(* com.wanted.momocity.review.application.service.ReviewCommandService.deleteReview(..))")
    public Object measureReviewDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopReviewDeleteTimer(sample);
        }
    }

    // 수강평 목록 조회 소요 시간 측정
    @Around("execution(* com.wanted.momocity.review.application.service.ReviewQueryService.getReviews(..))")
    public Object measureReviewList(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopReviewListTimer(sample);
        }
    }

    // 학습 진척도 조회 소요 시간 측정
    @Around("execution(* com.wanted.momocity.enrollment.application.service.EnrollmentQueryService.getProgress(..))")
    public Object measureEnrollmentProgress(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopEnrollmentProgressTimer(sample);
        }
    }

    // 챕터 등록 소요 시간 측정
    @Around("execution(* com.wanted.momocity.lecture.application.service.LectureCommandService.createChapter(..))")
    public Object measureChapterCreate(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopChapterCreateTimer(sample);
        }
    }

    // 챕터 동영상 등록 소요 시간 측정
    @Around("execution(* com.wanted.momocity.lecture.application.service.LectureCommandService.registerChapterVideo(..))")
    public Object measureChapterVideoRegister(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();

        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopChapterVideoRegisterTimer(sample);
        }
    }

    // 관리자 회원 목록 조회 소요 시간 측정
    @Around("execution(* com.wanted.momocity.user.application.service.UserQueryService.getAdminUserList(..))")
    public Object measureAdminUserList(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopAdminUserListTimer(sample);
        }
    }

    // 강사 승인 처리 소요 시간 측정
    @Around("execution(* com.wanted.momocity.user.application.service.UserCommandService.approve(..))")
    public Object measureTeacherApprove(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } finally {
            momoMetrics.stopTeacherApproveTimer(sample);
        }
    }

}
