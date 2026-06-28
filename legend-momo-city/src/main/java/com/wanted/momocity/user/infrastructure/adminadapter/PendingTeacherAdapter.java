package com.wanted.momocity.user.infrastructure.adminadapter;

import com.wanted.momocity.admin.application.port.PendingTeacherPort;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PendingTeacherAdapter implements PendingTeacherPort {

    private final SpringDataUserRepository springDataUserRepository;

    // 최대 limit개 만큼 대기 강사 정보 가져오기 userId, name, updatedAt(=requestedAt) 가져오기
    @Override
    public List<PendingTeacherItem> getPending(int limit) {
        return springDataUserRepository
                .findByRoleAndStatus(Role.TEACHER, Status.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(u -> new PendingTeacherItem(u.getId(), u.getName(), u.getUpdatedAt()))
                .toList();
    }
}