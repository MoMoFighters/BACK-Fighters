package com.wanted.momocity.report.infrastructure.adapter;

/* comment.
    UserReportListPort 구현체
    User BC 가 신고 목록을 요청할 때 report 테이블을 쿼리한다.
 */

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

    // UserReportListPort 인터페이스에 선언된 getReportsByUserId 를 여기서 실제로 구현한다.
    @Override
    public List<ReportInfo> getReportsByUserId(Long userId) {
        return reportRepository.findAllByReportedUserId(userId).stream()
                .map(e -> new ReportInfo(
                        ReportTargetType.valueOf(e.getTargetType()),
                        e.getReason(),
                        e.getCreatedAt(),
                        e.isResolved()
                ))
                .toList();
    }

}
