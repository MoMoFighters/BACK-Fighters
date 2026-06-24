package com.wanted.momocity.community.infrastructure.scheduler;

import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
* comment.
*  Community 도메인 하드딜리트 스케줄러
*  - Scheduled cron : 매일 자정 실행, 소프트딜리트 후 6개월 지난 데이터 하드딜리트
*  - post, comment, post_content : deleted_at 기준 6개월 경과
*  - post_content 먼저 삭제 (post 참조)
*  - comment 삭제 (post 참조)
*  - post 마지막 삭제
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityCleanupScheduler {

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final CommentRepository commentRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void hardDelete() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);

        // comment 삭제 (post 참조하므로)
        int deletedComments = commentRepository.hardDeleteByDeletedAtBefore(threshold);

        // post 마지막 삭제
        int deletedPosts = postRepository.hardDeleteByDeletedAtBefore(threshold);

        log.info("[Community] 하드딜리트 완료 | posts={}, comments={}",
                deletedPosts, deletedComments);
    }

}
