package com.wanted.momocity.notification.presentation.api;


import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.notification.application.command.ReadNotificationCommand;
import com.wanted.momocity.notification.application.command.RemoveNotificationCommand;
import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase.NotiView;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase.MainTotalCountsView;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase.PhoneAppCountsView;
import com.wanted.momocity.notification.presentation.api.request.ReadNotificationRequest;
import com.wanted.momocity.notification.presentation.api.request.RemoveNotificationRequest;
import com.wanted.momocity.notification.presentation.api.response.GetMainTotalCountsResponse;
import com.wanted.momocity.notification.presentation.api.response.GetNotificationResponse;
import com.wanted.momocity.notification.presentation.api.response.GetPhoneAppCountsResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    //쿼리(총 알림 개수, 알림 목록, 페이지 이동)
    private final NotificationQueryUseCase notificationQueryUseCase;
    //커맨드(알림 읽음, 알림 삭제)
    private final NotificationCommandUseCase notificationCommandUseCase;

    //알림 목록 조회
    @GetMapping("/api/v2/notice/notificationlist")
    @Operation(summary = "알림 목록", description = "로그인 유저에게 온 모든 알림 내역을 조회한다.")
    public ResponseEntity<ApiResponse<List<GetNotificationResponse>>> getNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        GetNotificationQuery query = new GetNotificationQuery(userId);
        List<NotiView> view = notificationQueryUseCase.getNotificationQueryHandle(query);

        //채팅방이 한개도 없을 때
        if (view.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "받은 알림이 없습니다.",
                    List.of()
            ));
        }

        //DTO 가공 변환
        List<GetNotificationResponse> responseData = view.stream()
                .map(GetNotificationResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "알림 목록 불러오기 성공",
                responseData
        ));
    }

    //메인페이지 종에 띄울 총 알림
    @GetMapping("/api/v2/notice/total-counts")
    @Operation(summary = "전체 알림 개수", description = "로그인 후 메인 페이지의 상단 종에 띄워질 총 알림 개수")
    public ResponseEntity<ApiResponse<GetMainTotalCountsResponse>> getMainTotalCounts(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        // 1. 쿼리 조립 및 유스케이스 실행
        GetMainTotalCountsQuery query = new GetMainTotalCountsQuery(userId);
        MainTotalCountsView view = notificationQueryUseCase.getMainTotalCountsQueryHandle(query);

        // 2. DTO 변환
        GetMainTotalCountsResponse responseData = new GetMainTotalCountsResponse(view.totalCount());

        // 3. 성공 공통 응답 반환
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "전체  알림 개수 조회 성공",
                responseData
        ));
    }

    //휴대폰 앱별 총 알림 개수(친구+메시지, 캘린더, 커뮤니티)
    @GetMapping("/api/v3/notice/app-counts")
    @Operation(summary = "휴대폰 속 앱별 알림 개수", description = "휴대폰 속 친구+메시지/캘린더/커뮤니티 앱에 띄워질 알림 개수")
    public ResponseEntity<ApiResponse<GetPhoneAppCountsResponse>> getPhoneAppCounts(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        // 1. 쿼리 조립 및 유스케이스 실행
        GetPhoneAppCountsQuery query = new GetPhoneAppCountsQuery(userId);
        PhoneAppCountsView view = notificationQueryUseCase.getPhoneAppCountsQueryHandle(query);

        // 2. DTO 변환
        GetPhoneAppCountsResponse responseData = new GetPhoneAppCountsResponse(
                view.totalMsgFriendCount(),
                view.calendarCount(),
                view.communityCount(),
                view.studyCount());

        // 3. 성공 공통 응답 반환
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "앱별 알림 개수 조회 성공",
                responseData
        ));
    }

    //알림 읽음
    @PatchMapping("/api/v2/notice/read")
    @Operation(summary = "알림 읽기", description = "알림 목록 하나 클릭 또는 일괄 읽음 처리를 한다.")
    public ResponseEntity<ApiResponse<Void>> readNotification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody ReadNotificationRequest request) {
        Long userId = userDetails.getUserId();

        // 1. 커맨드 객체 맵핑 및 서비스 호출
        notificationCommandUseCase.readNotificationCommandHandle(
                new ReadNotificationCommand(userId, request.targetId())
        );

        // 2. 200 OK 스펙 규격에 맞춰 빈 데이터 반환
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "알림을 성공적으로 읽었습니다.",
                null
        ));
    }

    //알림 삭제
    @DeleteMapping("/api/v2/notice/remove")
    @Operation(summary = "알림 삭제", description = "알림 목록 하나 클릭 또는 일괄 삭제 처리를 한다.")
    public ResponseEntity<ApiResponse<Void>> removeNotification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody RemoveNotificationRequest request) {
        Long userId = userDetails.getUserId();

        // 1. 커맨드 객체 맵핑 및 서비스 호출
        notificationCommandUseCase.removeNotificationCommandHandle(
                new RemoveNotificationCommand(userId, request.targetId())
        );

        // 2. 200 OK 스펙 규격에 맞춰 빈 데이터 반환
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "알림을 성공적으로 삭제했습니다.",
                null
        ));
    }
}
