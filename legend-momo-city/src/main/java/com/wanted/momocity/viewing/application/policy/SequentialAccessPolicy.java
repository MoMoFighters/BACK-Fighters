package com.wanted.momocity.viewing.application.policy;

import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.domain.exception.ViewingAccessDeniedException;
import com.wanted.momocity.viewing.domain.exception.ViewingNotFoundException;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  순차 시청 제한 정책 클래스
 *  -> 이전 챕터 완료 여부 확인
 *  -> 미완료 시 다음 챕터 접근 불가
 *  -
 *  [사용하는 Service]
 *  -> ViewingQueryService: getStreamingUrl
 */

@Component
@RequiredArgsConstructor
public class SequentialAccessPolicy {

    private final ChapterPort chapterPort;
    private final LearningHistoryRepository learningHistoryRepository;

    public void ensureSequentialAccess(Long userId, Long lectureId, Long chapterId) {

        // 관리자/강사는 순차 시청 제한 없음
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_INSTRUCTOR"))) {
            return;
        }

        Chapter chapter = chapterPort.findById(chapterId);


        // 첫 번째 챕터는 제한 없음
        if (chapter.getOrderNo() == 1) return;

        // 이전 챕터 조회
        Chapter prevChapter = chapterPort
                .findByLectureIdAndOrderNo(lectureId, chapter.getOrderNo() - 1)
                .orElseThrow(() -> new ViewingNotFoundException(
                        "이전 챕터를 찾을 수 없습니다."));

        // 이전 챕터 시청 기록 조회
        LearningHistory prevHistory = learningHistoryRepository
                .findByUserIdAndChapterId(userId, prevChapter.getId())
                .orElseThrow(() -> new ViewingAccessDeniedException(
                        "이전 챕터를 먼저 수강해주세요."));

        // 이전 챕터 미완료 시 접근 불가
        if (!prevHistory.isCompleted()) {
            throw new ViewingAccessDeniedException("이전 챕터를 먼저 수강해주세요.");
        }
    }

    }
