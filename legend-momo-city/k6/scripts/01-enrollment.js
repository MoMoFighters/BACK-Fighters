// k6/scripts/01-enrollment.js
// ------------------------------------------------------------
// 목적:
// - 수강신청 러시 트래픽에서 DB 쓰기 성능과 중복 방지 로직을 확인합니다.
//
// 관찰할 것:
// - k6: enrollment 타입 p95, http_req_failed
// - Prometheus: http_server_requests_seconds (enrollment 엔드포인트)
// - Loki: 수강신청 성공/중복 로그 비율
//
// 실행:
//   k6 run k6/scripts/01-enrollment.js
//   RESULT_NAME=enrollment-rush k6 run k6/scripts/01-enrollment.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { enrollLecture } from '../lib/client.js';
import { vuToken, randomLectureId, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        enrollment_rush: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 20 },  // 워밍업: 20명까지 증가
                { duration: '30s', target: 50 },  // 피크: 50명 유지 (수강신청 러시)
                { duration: '10s', target: 0  },  // 종료
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        // enrollment 타입 요청만 별도로 기준을 둡니다.
        // 중복 수강신청(400/409)은 실패로 집계되지 않으므로 순수 서버 처리 속도를 봅니다.
        'http_req_duration{type:enrollment}': ['p(95)<2000'],
    },
};

export default function () {
    // VU 번호 기반 토큰 배정: 같은 VU는 항상 같은 학생으로 동작합니다.
    // 이를 통해 중복 수강신청 거절 패턴을 자연스럽게 재현합니다.
    const token = vuToken();
    const lectureId = randomLectureId();

    enrollLecture(token, lectureId);

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('01-enrollment');
