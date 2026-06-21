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
*  -
*  - @Scheduled(cron = "0 0 9 * * *") : 매일 오전 9시 실행
*  - 오늘 날짜 기준 Todo / Memo 조회, notification 테이블에 저장
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

        for (Calendar calendar : todayCalendars) {
            if (calendar.getCategory() == Calendar.Category.TODO) {
                notificationHandlerService.createTodoReminderNotification(
                        calendar.getUserId(), calendar.getTitle());
            } else {
                notificationHandlerService.createMemoReminderNotification(
                        calendar.getUserId(), calendar.getTitle());
            }
        }

        log.info("[CalendarNotificationScheduler] 알림 발송 완료 | date={}, count={}",
                today, todayCalendars.size());
    }

}
