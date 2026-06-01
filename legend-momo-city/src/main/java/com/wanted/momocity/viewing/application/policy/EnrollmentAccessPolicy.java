package com.wanted.momocity.viewing.application.policy;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.exception.ViewingAccessDeniedException;
import com.wanted.momocity.viewing.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
* comment.
*  enrollment 컨텍스트를 참조해서 수강신청 여부를 확인하는 정책 클래스
*  enrollment 는 외부 바운디드 컨텍스트라서 Service 에서 참조하지 않고 Policy 로 분리
*  -
*  [사용하는 Service]
*  - ViewingService: getStreamingUrl, getLectureMeta, getChapterResume
*  - ProgressService: saveProgress, getTotalProgress, getChapterProgress
 * */

@Component
@RequiredArgsConstructor
public class EnrollmentAccessPolicy {

    private final EnrollmentPort enrollmentPort;
    private final LecturePort lecturePort;

    public void ensureEnrolled(Long userId, Long lectureId) {
        // 현재 사용자 권한 확인
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();

        // 관리자는 모든 강의 접근 가능
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        // 강사는 본인 강의 접근 가능
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTRUCTOR"))) {
            // LecturePort 로 강의 조회 후 teacherId 확인
            Lecture lecture = lecturePort.findById(lectureId);
            if (lecture.getTeacherId().equals(userId)) return;
        }

        // 학생은 수강 신청 확인
        enrollmentPort.findByUserIdAndLectureId(userId, lectureId)
                .orElseThrow(() -> new ViewingAccessDeniedException(
                        "수강 신청된 강의만 시청할 수 있습니다."
                ));
    }

}
