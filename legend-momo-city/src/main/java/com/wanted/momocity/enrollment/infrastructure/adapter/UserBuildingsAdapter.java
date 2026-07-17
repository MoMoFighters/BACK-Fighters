package com.wanted.momocity.enrollment.infrastructure.adapter;

import com.wanted.momocity.enrollment.infrastructure.persistence.SpringDataBuildingRepository;
import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.application.port.GetUserBuildingsPort;
import com.wanted.momocity.user.domain.model.BuildingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class UserBuildingsAdapter implements GetUserBuildingsPort {

    private final SpringDataBuildingRepository springDataBuildingRepository;
    private static final String BUILDING_IMAGE_BASE_URL = "https://momocity-media.s3.ap-northeast-2.amazonaws.com/building";


    @Override
    public List<BuildingInfo> getUserBuildings(Long userId) {
        return springDataBuildingRepository.findByUserId(userId)
                .stream()
                .map(building -> new BuildingInfo(
                        building.getCategory(),
                        building.getPosition(),
                        building.getLevel(),
                        buildBuildingUrl(building.getCategory(), building.getLevel()) // 카테고리와 레벨로 이미지 URL 만들어서 응답
                ))
                .toList();
    }

    private String buildBuildingUrl(Category category, Integer level) {
        String categoryPath = category.name().toLowerCase(Locale.ROOT); // ENUM 값 소문자로 바꾸기
        return BUILDING_IMAGE_BASE_URL + "/" + categoryPath + "/level-" + level + ".png";
    }
}
