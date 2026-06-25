package com.wanted.momocity.user.application.port;

import com.wanted.momocity.user.domain.model.ReportInfo;

import java.util.List;

public interface UserReportListPort {
    List<ReportInfo> getReportsByUserId(Long userId);

}
