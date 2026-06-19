// k6/scripts/02-lecture-list.js
// ------------------------------------------------------------
// 목적:
// - 강의 목록 조회 API의 읽기 집중 부하에서 응답 성능을 측정합니다.
// - 카테고리 필터, 키워드 검색, 비로그인/로그인 혼합 패턴을 포함합니다.
//
// 관찰할 것:
// - k6: lecture-list 타입 p95
// - Prometheus: http_server_requests_seconds (GET /api/v1/lectures)
// - Loki: 느린 조회 쿼리 로그
//
// 실행:
//   k6 run k6/scripts/02-lecture-list.js
//   RESULT_NAME=lecture-list-peak k6 run k6/scripts/02-lecture-list.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getLectures } from '../lib/client.js';
import { randomStudentToken, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        lecture_list_load: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 50  },  // 워밍업
                { duration: '30s', target: 100 },  // 100명 유지
                { duration: '20s', target: 200 },  // 200명 피크
                { duration: '10s', target: 0   },  // 종료
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        // 읽기 API는 쓰기보다 빨라야 합니다. p95 1초 기준을 둡니다.
        'http_req_duration{type:lecture-list}': ['p(95)<1000'],
    },
};

const CATEGORIES = ['FITNESS', 'STUDY', 'COOK', 'BEAUTY', 'ART', null];
const KEYWORDS    = ['홈트', '필라테스', '자바', '영어', null];

export default function () {
    const category = CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)];
    const keyword  = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    const page     = Math.floor(Math.random() * 3) + 1;

    // 비로그인 70% / 학생 로그인 30% 혼합으로 실제 서비스 패턴을 흉내냅니다.
    const token = Math.random() < 0.3 ? randomStudentToken() : null;

    getLectures({ category, keyword, page }, token);

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('02-lecture-list');
