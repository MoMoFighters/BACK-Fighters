package com.wanted.cleanarchitecture.catalog.infrastructure.event;

import com.wanted.cleanarchitecture.catalog.domain.event.CoursePublishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CourseLifecycleEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CourseLifecycleEventHandler.class);

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CoursePublishedEvent event) {
        log.info("course-published courseId={}", event.courseId());
    }
}
