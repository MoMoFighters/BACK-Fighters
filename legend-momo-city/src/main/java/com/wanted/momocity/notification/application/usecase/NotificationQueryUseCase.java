package com.wanted.momocity.notification.application.usecase;

import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationQueryUseCase {

    //알림 목록
    List<NotiView> getNotificationQueryHandle(GetNotificationQuery query);

    record NotiView(
            Long notificationId,
            String type,
            String message,
            Boolean isRead,
            Long refId,
            LocalDateTime createdAt
    ) {}

    //메인페이지 종에 띄울 총 알림 개수
    MainTotalCountsView getMainTotalCountsQueryHandle(GetMainTotalCountsQuery query);

    record MainTotalCountsView(
            Long totalCount
    ) {}

    //휴대폰 속 앱별 알림 개수
    PhoneAppCountsView getPhoneAppCountsQueryHandle(GetPhoneAppCountsQuery query);

    record PhoneAppCountsView(
            Long totalMsgFriendCount,
            Long calendarCount,
            Long communityCount,
            Long studyCount
    ) {}
}
