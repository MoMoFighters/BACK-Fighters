package com.wanted.momocity.user.presentation.api.response;

import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.domain.model.Membership;
import com.wanted.momocity.user.domain.model.ReportInfo;
import com.wanted.momocity.user.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(
        String email,
        Role role,
        LocalDateTime createdAt,
        Category category,
        String name,
        Membership membership,
        LocalDateTime membershipStart,
        Long suspensionCount,
        List<ReportInfo> reports
) {
}
