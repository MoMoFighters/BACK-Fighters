package com.wanted.momocity.enrollment.application.service;

import com.wanted.momocity.auth.domain.exception.UserNotFoundException;
import com.wanted.momocity.enrollment.application.query.EnrollmentQuery;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.enrollment.domain.exception.BuildingSelfAccessException;
import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.enrollment.domain.repository.BuildingRepository;
import com.wanted.momocity.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.momocity.enrollment.presentation.api.response.EnrollmentProgressResponse;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.UserRepository;
import com.wanted.momocity.viewing.application.port.CategoryProgressInfo;
import com.wanted.momocity.viewing.application.port.CategoryProgressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnrollmentQueryService implements EnrollmentQueryUsecase {

    private final BuildingRepository buildingRepository;

    // 카테고리 계산 포트
    private final CategoryProgressPort categoryProgressPort;

    // 수강신청 정보를 조회하는 도메인 Repository입니다.
    private final EnrollmentRepository enrollmentRepository;

    private final UserRepository userRepository;

    private final FriendRepository friendRepository;

    // S3 빌딩 건물 기본 경로를 상수로 둔다
    private static final String BUILDING_IMAGE_BASE_URL = "https://momocity-media.s3.ap-northeast-2.amazonaws.com/building";

    @Override
    public List<RenderingBuildingsView> userBuildingInfo(Long userId) {

        log.info("내 건물 목록 조회 시작 - userId={}", userId);
        return buildingRepository.findByUserId(userId)
                .stream()
                .map(building -> new RenderingBuildingsView(
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

    // 학습 진척도 조회
    @Override
    public EnrollmentProgressResponse getProgress(
            EnrollmentQuery.GetEnrollmentProgressQuery query
    ) {

        long startTime = System.currentTimeMillis();
        log.info(" 학습 진척도 조회 시작 - userId={}, category={}", query.userId(), query.category());
        // category 값을 검증하고 정리
        String category = normalizeCategory(query.category());

        // 기존 viewing 쪽 진척도 계산 로직 재사용
        CategoryProgressInfo progressInfo =
                categoryProgressPort.getCategoryProgress(query.userId(), category);

        // category가 없으면 내가 신청한 전체 강의 평균 진척도만 내린다.
        if (category == null) {
            long elapsedTime = System.currentTimeMillis()-startTime;
            log.info("전체 학습 진척도 조회 완료 - userId={}, myTotalProgress={}, elapsedTime={}",
                    query.userId(),
                    progressInfo.myTotalProgress(),
                    elapsedTime
                    );

            return new EnrollmentProgressResponse(
                    progressInfo.myTotalProgress(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        // category가 있으면 해당 카테고리 건물을 조회합니다.
        Building building = findBuildingByCategory(query.userId(), category);

        // 해당 카테고리에서 완료한 강의 수를 조회합니다.
        int completedLectureCount = countCompletedLectures(
                query.userId(),
                category
        );

        // 해당 카테고리 건물 유무 확인
        String buildingUrl = building == null ? null
                // 건물이 있다면 카테고리와 레벨로 S3 이미지 url 생성
                : createBuildingUrl(category, building.getLevel());

        long elapsedTime = System.currentTimeMillis()-startTime;
        log.info(" 카테고리 학습 진척도 조회 완료 - userId={}, category={}, progress={}, buildingLevelUrl={}, buildingUrl={}, elapsedTime={}ms",
                query.userId(),
                category,
                progressInfo.myTotalProgress(),
                building == null ? null : building.getLevel(),
                buildingUrl,
                elapsedTime
                );

        // category가 있으면 카테고리 진척도와 건물 정보를 함께 내려준다.
        return new EnrollmentProgressResponse(
                null,
                progressInfo.myTotalProgress(),
                building == null ? null : building.getLevel(),
                building == null ? null : calculateCurrentExp(completedLectureCount),
                building == null ? null : calculateTotalExp(),
                buildingUrl
        );
    }

    // 카테고리와 레벨로 S3 건물 이미지 url 생성
    private String createBuildingUrl(String category, Integer level) {
        // S3 버킷 Url
        String baseUrl = "https://momocity-meaid.s3.ap-northeast-2.amazonaws.com";
        // ENUM 문자열을 소문자로 지정
        String lowerCategory = category.toLowerCase();
        // 최종 건물 이미지 URL 반환
        return baseUrl + "/building/" + lowerCategory + "/level-" + level + ".png";
    }

    // 사용자 건물 중 category에 해당하는 건물을 찾습니다.
    private Building findBuildingByCategory(
            Long userId,
            String category
    ) {
        // userId 기준 건물 목록을 조회합니다.
        return buildingRepository.findByUserId(userId)
                .stream()
                // 건물 category와 요청 category가 같은 것만 찾습니다.
                .filter(building -> building.getCategory() == Category.valueOf(category))
                // 첫 번째 일치 건물을 반환합니다.
                .findFirst()
                // 없으면 null을 반환합니다.
                .orElse(null);
    }

    // 해당 category에서 완료한 강의 수를 조회합니다.
    private int countCompletedLectures(
            Long userId,
            String category
    ) {

        return enrollmentRepository.countCompletedLecturesByUserIdAndCategory(
                userId,
                category
        );
    }

    // category 값을 검증하고 정리
    private String normalizeCategory(String category) {

        // category가 없으면 전체 기준 조회
        if (category == null || category.isBlank()) {
            return null;
        }

        // enum 값은 대문자 유지 정책이므로 그대로 검증
        try {
            Category.valueOf(category);
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("잘못된 카테고리입니다.");
        }
        return category;
    }

    // 건물 현재 경험치
    private Integer calculateCurrentExp(int completedLectureCount) {
        // 20개 이상 완료하면 level3
        if(completedLectureCount >= 20) {
            return 1000;
        }

        // 강의 1개 완료 당 100xp로 계산
        return (completedLectureCount % 10) * 100;
    }

    // 건물 전체 필요 경험치
    private Integer calculateTotalExp() {
        return  1000;
    }

    @Override
    public FriendBuildingsView friendBuildingInfo(Long loginUserId, Long targetUserId) {

        long startTime = System.currentTimeMillis();
        log.info("친구 마을 건물 조회 시작 - loginUserId={}, targetUserId={}",
                loginUserId,
                targetUserId
                );

        if (loginUserId.equals(targetUserId)) {
            log.warn("친구 마을 건물 조회 실패 - 본인 ID 요청, loginUserId={}, targetUserId={}",
                    loginUserId,
                    targetUserId
            );
            throw new BuildingSelfAccessException("사용자 본인이기 때문에 메인페이지로 이동합니다.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 두 사용자 사이의 친구 관계 조회
        boolean isFriend = friendRepository.findAnyRelationBetween(loginUserId, targetUserId)
                // 상태가 친구만 허용
                .filter(friend -> "FRIEND".equals(friend.getStatus()))
                .isPresent();

        if (!isFriend) { // 두 사용자가 친구 관계가 아닌지 확인
            log.warn("친구 마을 건물 조회 실패 - 친구 관계 아님, loginUserId={}, targetUserId={}",
                    loginUserId,
                    targetUserId
            );
            throw new DomainRuleViolationException("친구 관계인 사용자만 방문할 수 있습니다.");
        }

        List<RenderingBuildingsView> buildings = userBuildingInfo(targetUserId);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("친구 마을 건물 조회 성공 - loginUserId={}, targetUserId={}, buildingCount={}, elapsedTime={}ms",
                loginUserId,
                targetUserId,
                buildings.size(),
                elapsedTime
                );

        return new FriendBuildingsView(
                targetUser.getNickname(),
                buildings
        );
    }
}
