package com.wanted.momocity.user.application.port;

import com.wanted.momocity.user.domain.model.ReportInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserReportListAdapter implements UserReportListPort{
    @Override
    public List<ReportInfo> getReportsByUserId(Long userId) {
        return List.of();
    }
}
