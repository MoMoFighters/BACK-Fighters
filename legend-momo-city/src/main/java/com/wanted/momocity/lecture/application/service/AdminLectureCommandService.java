package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.AdminChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.usecase.AdminLectureCommandUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.AdminChangeLectureStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 강의 명령을 처리하는 Application Service 강의 승인/거절 상태 변경을 담당
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminLectureCommandService implements AdminLectureCommandUseCase {

    private final LectureRepository lectureRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public AdminChangeLectureStatusResponse changeLectureStatus(AdminChangeLectureStatusCommand command) {

        /* comment
         * 상태를 변경할 강의를 조회한다.
         * 존재하지 않으면 강의 없음 예외를 던진다.
         */
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 승인(ACTIVE)으로 변경할 때만 강의 공개 조건을 검증
        if (command.lectureStatus() == LectureStatus.ACTIVE) {
            validateLectureReadyForApproval(command.lectureId());
        }

        // 도메인 모델에서 강의 상태를 변경
        LectureAggregate changedLecture =
                lecture.changeStatus(command.lectureStatus());

        // 변경된 강의 상태를 저장
        LectureAggregate savedLecture =
                lectureRepository.save(changedLecture);

        // 관리자 승인/거절 결과를 추적하기 위한 로그
        // ACTIVE는 승인, HOLD는 거절 상태로 사용되므로 adminId와 변경된 상태를 남긴다.
        log.info("관리자 강의 상태 변경 완료 - adminId={}, lectureId={}, lectureStatus={}",
                command.adminId(),
                savedLecture.getId(),
                savedLecture.getStatus());

        // 변경 결과를 응답 DTO로 변환
        return AdminChangeLectureStatusResponse.from(savedLecture);
    }

    /* comment
     * 강의를 ACTIVE로 승인하기 전에 공개 가능한 상태인지 검증한다.
     * 조건:
     * 1. 챕터가 최소 1개 이상 있어야 한다.
     * 2. 모든 챕터에 동영상 URL이 있어야 한다.
     * 3. 모든 챕터의 동영상 상태가 READY여야 한다.
     */
    private void validateLectureReadyForApproval(Long lectureId) {
        int chapterCount = chapterRepository.countByLectureId(lectureId);

        if (chapterCount < 1) {
            throw new DomainRuleViolationException("강의를 승인하려면 최소 1개 이상의 챕터가 필요합니다.");
        }

        if (chapterRepository.existsByLectureIdAndVideoUrlIsNull(lectureId)) {
            throw new DomainRuleViolationException("강의를 승인하려면 모든 챕터에 동영상이 등록되어야 합니다.");
        }

        if (chapterRepository.existsByLectureIdAndVideoStatusNot(lectureId, VideoStatus.READY)) {
            throw new DomainRuleViolationException("강의를 승인하려면 모든 동영상이 READY 상태여야 합니다.");
        }
    }
}