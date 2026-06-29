package com.wanted.momocity.report.infrastructure.adapter;

/* comment.
    UserReportListPort 구현체
    User BC 가 신고 목록을 요청할 때 report 테이블을 쿼리한다.
 */

import com.wanted.momocity.report.application.port.ChatContentPort;
import com.wanted.momocity.report.application.port.CommentContentPort;
import com.wanted.momocity.report.application.port.ReviewContentPort;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import com.wanted.momocity.report.infrastructure.persistence.SpringDataReportRepository;
import com.wanted.momocity.user.application.port.UserReportListPort;
import com.wanted.momocity.user.domain.model.ReportInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserReportListAdapter implements UserReportListPort {

    private final SpringDataReportRepository reportRepository;

    // targetType 기준으로 신고 대상 콘텐츠 원문을 조회하는 포트들
    private final ReviewContentPort reviewContentPort;
    private final CommentContentPort commentContentPort;
    private final ChatContentPort chatContentPort;

    // UserReportListPort 인터페이스에 선언된 getReportsByUserId 를 여기서 실제로 구현한다.
    @Override
    public List<ReportInfo> getReportsByUserId(Long userId) {
        return reportRepository.findAllByReportedUserId(userId).stream()
                .map(e -> {
                    ReportTargetType targetType = ReportTargetType.valueOf(e.getTargetType());

                    // reason 대신 targetType 기준으로 실제 신고 대상 콘텐츠 원문 조회
                    String targetContent = switch (targetType) {
                        case REVIEW  -> reviewContentPort.getContentById(e.getTargetId());
                        case COMMENT -> commentContentPort.getContentById(e.getTargetId());
                        case CHAT    -> chatContentPort.getContentById(e.getTargetId());
                        default -> null;
                    };

                    return new ReportInfo(
                            targetType,
                            targetContent,
                            e.getCreatedAt(),
                            e.isResolved()
                    );
                })
                .toList();
    }

}
