// k6/scripts/00-smoke-check.js
// ------------------------------------------------------------
// 목적:
// - 본격 부하테스트 전에 서버가 정상 응답하는지 확인합니다.
// - 성능 한계를 찾는 테스트가 아니라 "테스트 가능한 상태인지" 확인하는 테스트입니다.
//
// 실행:
//   k6 run k6/scripts/00-smoke-check.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getLectures, enrollLecture, saveProgress } from '../lib/client.js';
import { vuToken, randomLectureId, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    vus: 1,
    iterations: 3,
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<2000'],
    },
};

export default function () {
    const token = vuToken();

    // 1. 강의 목록 조회로 DB 연결과 기본 API를 확인합니다.
    getLectures({ page: 1 }, token);

    // 2. 수강신청으로 쓰기 API를 확인합니다.
    //    이미 신청된 경우 400/409 응답도 정상으로 처리합니다.
    enrollLecture(token, 1);

    // 3. 진척도 저장으로 PATCH API를 확인합니다.
    //    student1~4는 lecture 1에 미리 수강신청된 상태입니다.
    saveProgress(token, 1, 1, 100);

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('00-smoke-check');
