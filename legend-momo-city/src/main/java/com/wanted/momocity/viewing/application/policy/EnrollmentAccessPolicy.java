package com.wanted.momocity.viewing.application.policy;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
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

    public void ensureEnrolled(Long userId, Long lectureId) {
        enrollmentPort.findByUserIdAndLectureId(userId, lectureId)
                .orElseThrow(() -> new DomainRuleViolationException(
                        "수강 신청된 강의만 시청할 수 있습니다."
                ));
    }

}
