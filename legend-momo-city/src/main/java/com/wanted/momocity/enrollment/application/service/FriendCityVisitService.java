package com.wanted.momocity.enrollment.application.service;

import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.enrollment.application.usecase.FriendCityVisitUseCase;
import com.wanted.momocity.global.application.point.AddOrderHistory;
import com.wanted.momocity.global.application.point.PointChange;
import com.wanted.momocity.order.application.port.CheckPointPort;
import com.wanted.momocity.order.domain.exception.InsufficientPointException;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendCityVisitService implements FriendCityVisitUseCase {

    // 친구 도시 방문 한 번에 사용하는 버스 요금
    private static final Long BUS_FARE = 5L;

    private final EnrollmentQueryUsecase enrollmentQueryUsecase;

    private final CheckPointPort checkPointPort;

    private final PointChange pointChange;

    private final AddOrderHistory addOrderHistory;

    @Override
    public EnrollmentQueryUsecase.FriendBuildingsView visitFriendCity(Long loginUserId, Long targetUserId) {
        // 본인 방문, 존재하지 않는 사용자, 친구가 아닌 경우를 먼저 검증
        EnrollmentQueryUsecase.FriendBuildingsView friendBuildings =
                enrollmentQueryUsecase.friendBuildingInfo(
                        loginUserId,
                        targetUserId
                );

        if (!checkPointPort.isPointAble(loginUserId, BUS_FARE)) {
            throw new InsufficientPointException(
                    "친구 도시 방문에 필요한 포인트가 부족합니다."
            );
        }

        // 검증이 끝난 로그인 사용자의 포인트를 5포인트 차감
        pointChange.usePoint(loginUserId, BUS_FARE);

        // 차감한 포인트를 BUS 사용 이력으로 저장
        addOrderHistory.saveOrderHistory(
                loginUserId,
                Reason.BUS,
                Type.USED,
                BUS_FARE
        );

        // 포인트 차감이 완료되면 조회한 친구 도시 정보를 반환
        return friendBuildings;
    }
}
