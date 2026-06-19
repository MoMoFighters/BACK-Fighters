// k6/scripts/03-mixed.js
// ------------------------------------------------------------
// 목적:
// - 강의 목록 조회(읽기)와 수강신청(쓰기)이 동시에 발생하는 혼합 트래픽을 시뮬레이션합니다.
// - 읽기가 쓰기의 영향을 받아 느려지는지 확인합니다.
//
// 관찰할 것:
// - k6: 읽기/쓰기 타입별 p95 비교
// - Prometheus: JVM 스레드, 커넥션 풀 사용량 변화
// - Loki: 동시 요청에서 에러 발생 여부
//
// 실행:
//   k6 run k6/scripts/03-mixed.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getLectures, enrollLecture } from '../lib/client.js';
import { vuToken, randomLectureId, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        // 읽기 VU: 100명이 강의 목록 조회를 계속 실행합니다.
        read_users: {
            executor: 'constant-vus',
            vus: 100,
            duration: '60s',
            exec: 'readLectures',
        },
        // 쓰기 VU: 30명이 수강신청을 계속 실행합니다.
        // 읽기보다 VU가 적은 이유는 실제 서비스에서 조회가 수강신청보다 훨씬 많기 때문입니다.
        write_users: {
            executor: 'constant-vus',
            vus: 30,
            duration: '60s',
            exec: 'enrollLectures',
        },
    },
    thresholds: {
        'http_req_duration{type:lecture-list}': ['p(95)<1000'],
        'http_req_duration{type:enrollment}':   ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

export function readLectures() {
    getLectures({ page: 1 });
    sleep(randomSleepSeconds());
}

export function enrollLectures() {
    const token = vuToken();
    enrollLecture(token, randomLectureId());
    sleep(randomSleepSeconds());
}

// 03-mixed.js는 exec으로 함수를 직접 지정하므로 default는 사용하지 않습니다.
export default function () {}

export const handleSummary = createSummaryHandler('03-mixed');
