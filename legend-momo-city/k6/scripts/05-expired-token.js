// k6/scripts/05-expired-token.js
// ------------------------------------------------------------
// 목적:
// - 임시비밀번호로 발급받은 토큰이 만료된 상태에서 API 요청 시
//   서버가 401을 안정적으로 반환하는지 확인합니다.
// - 의도된 실패를 Prometheus와 Loki에서 추적하는 흐름을 연습합니다.
//
// 관찰할 것:
// - k6: checks rate (401 응답 비율)
// - Prometheus: http_server_requests_seconds{status="401"}
// - Loki: event=api_error reason=unauthorized
//
// 실행:
//   k6 run k6/scripts/05-expired-token.js
// ------------------------------------------------------------

import { check, sleep } from 'k6';
import { requestWithExpiredToken } from '../lib/client.js';
import { randomLectureId, randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        expired_token_error: {
            // constant-vus는 오류 로그를 안정적으로 계속 만들기 위한 단순한 실행 모델입니다.
            // ramping-vus가 아닌 이유는 부하 크기보다 "401이 일관되게 반환되는가"를 보는 게 목적이기 때문입니다.
            executor: 'constant-vus',
            vus: 50,
            duration: '1m',
        },
    },
    thresholds: {
        // 이 시나리오는 401을 의도적으로 만들기 때문에 http_req_failed 기준을 두지 않습니다.
        // 대신 check에서 401이 일관되게 반환되는지 확인합니다.
        // checks rate가 95% 이상이면 "의도한 실패가 의도한 방식으로 발생했다"고 판단합니다.
        checks: ['rate>0.95'],
    },
};

export default function () {
    const lectureId = randomLectureId();

    const res = requestWithExpiredToken(lectureId, 1, 50);

    // 500이 섞여 나오면 토큰 검증 로직에서 예외 처리가 누락된 것입니다.
    check(res, {
        'expired token returns 401': (r) => r.status === 401,
    });

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('05-expired-token');