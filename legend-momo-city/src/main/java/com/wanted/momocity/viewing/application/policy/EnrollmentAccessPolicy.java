package com.wanted.momocity.viewing.application.policy;

import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.application.port.UserPort;
import com.wanted.momocity.viewing.domain.exception.ViewingAccessDeniedException;
import com.wanted.momocity.viewing.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
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
    private final UserPort userPort;

    public void ensureEnrolled(Long userId, Long lectureId) {

        /*
         * SecurityContextHolder 제거
         * → STOMP 스레드에서 SecurityContext 가 없어서 NPE 발생
         * → userId 로 직접 DB 조회해서 role 확인하는 방식으로 변경
         */
        User user = userPort.findById(userId)
                .orElseThrow(() -> new DomainRuleViolationException(
                        "유저를 찾을 수 없습니다."
                ));

        // 관리자는 모든 강의 접근 가능
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // 강사는 본인 강의 접근 가능
        if (user.getRole() == Role.TEACHER) {
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
