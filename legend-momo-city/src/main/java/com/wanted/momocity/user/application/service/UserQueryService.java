package com.wanted.momocity.user.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.Building;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.BuildingRepository;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final UserPolicy userPolicy;


    @Override
    public UserDetailView userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new DomainRuleViolationException("사용자를 찾을 수 없습니다."));

        return new UserDetailView(
                user.getProfileImageUrl(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getBirth()
        );
    }

    @Override
    public void checkNickname(String nickname) {
        userPolicy.nicknamePolicy(nickname);
    }

    @Override
    public RenderingBuildingsView userBuildingInfo(Long userId) {
        return buildingRepository.findByUserId(userId)
                .map(building -> new RenderingBuildingsView(
                        building.getCategory(),
                        building.getPosition(),
                        building.getLevel()
                ))
                .orElse(new RenderingBuildingsView(null, null, null));
    }
}
