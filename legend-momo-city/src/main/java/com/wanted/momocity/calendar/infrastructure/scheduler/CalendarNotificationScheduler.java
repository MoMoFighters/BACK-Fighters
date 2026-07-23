package com.wanted.momocity.calendar.infrastructure.scheduler;

import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.notification.application.service.NotificationHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  Calendar 알림 스케줄러
*  - @Scheduled(cron = "0 0 9 * * *") : 매일 오전 9시 실행
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarNotificationScheduler {

    private final CalendarRepository calendarRepository;
    private final NotificationHandlerService notificationHandlerService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendCalendarNotifications() {
        LocalDate today = LocalDate.now();

        // 오늘 날짜 기준 전체 조회
        List<Calendar> todayCalendars = calendarRepository.findAllByDate(today);

        int successCount = 0;
        for (Calendar calendar : todayCalendars) {
            try {
                sendOne(calendar);
                successCount++;
            } catch (Exception e) {
                // 한 건 실패해도 나머지는 계속 발송 - 배치 특성상 개별 실패가 전체를 막으면 안 됨
                log.warn("[CalendarNotificationScheduler] 알림 발송 실패 | calendarId={}, userId={}, message={}",
                        calendar.getId(), calendar.getUserId(), e.getMessage());
            }
        }

        log.info("[CalendarNotificationScheduler] 알림 발송 완료 | date={}, 대상={}, 성공={}",
                today, todayCalendars.size(), successCount);

    }

    // 카테고리에 따라 Todo/Memo 알림으로 분기
    private void sendOne(Calendar calendar) {
        if (calendar.getCategory() == Calendar.Category.TODO) {
            notificationHandlerService.createTodoNotification(
                    calendar.getUserId(), calendar.getId(), calendar.getTitle());
        } else {
            notificationHandlerService.createMemoNotification(
                    calendar.getUserId(), calendar.getId(), calendar.getTitle());
        }
    }

}
