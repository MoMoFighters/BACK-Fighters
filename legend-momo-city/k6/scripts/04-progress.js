// k6/scripts/04-progress.js
// ------------------------------------------------------------
// 목적:
// - 학생들이 강의를 동시에 시청하며 5~10초 주기로 진척도를 저장할 때의 쓰기 성능을 측정합니다.
//
// 전제:
// - student1~4가 lecture 1에 수강신청된 상태여야 합니다.
//   (00-smoke-check 또는 01-enrollment 실행 이후 또는 DB에 직접 삽입)
//
// 관찰할 것:
// - k6: progress 타입 p95
// - Prometheus: DB 커넥션 사용량 (진척도 저장은 매번 upsert를 수행합니다)
// - Loki: 챕터 완료 이벤트 로그 발생 여부
//
// 실행:
//   k6 run k6/scripts/04-progress.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { saveProgress } from '../lib/client.js';
import { vuToken, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        progress_save: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 20  },  // 워밍업
                { duration: '40s', target: 50  },  // 50명 유지
                { duration: '10s', target: 100 },  // 100명 피크 (동시 시청자 시뮬레이션)
                { duration: '10s', target: 0   },  // 종료
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        'http_req_duration{type:progress}': ['p(95)<1500'],
    },
};

// 진척도 저장 대상 챕터 목록입니다.
// lecture 1의 챕터 1(600초), 챕터 2(900초)를 사용합니다.
const CHAPTERS = [
    { lectureId: 1, chapterId: 1, durationSec: 600 },
    { lectureId: 1, chapterId: 2, durationSec: 900 },
];

export default function () {
    const token   = vuToken();
    const chapter = CHAPTERS[Math.floor(Math.random() * CHAPTERS.length)];

    // 재생 위치를 랜덤으로 설정합니다.
    // 0~95% 구간에서 선택해 챕터 완료 이벤트가 적당히 발생하도록 합니다.
    const playbackSeconds = Math.floor(Math.random() * chapter.durationSec * 0.95);

    saveProgress(token, chapter.lectureId, chapter.chapterId, playbackSeconds);

    // 실제 플레이어는 5~10초마다 저장 요청을 보내므로 sleep 범위를 맞춥니다.
    sleep(5 + Math.random() * 5);
}

export const handleSummary = createSummaryHandler('04-progress');
