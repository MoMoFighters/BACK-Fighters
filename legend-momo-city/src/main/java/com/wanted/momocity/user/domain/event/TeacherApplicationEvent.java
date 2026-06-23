package com.wanted.momocity.user.domain.event;

import com.wanted.momocity.user.domain.model.Status;

public record TeacherApplicationEvent(
        String email,
        Status status,
        String reason
) {}