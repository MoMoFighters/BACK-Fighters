// k6/scripts/07-dashboard-after.js
// ------------------------------------------------------------
// 목적:
// - AdminDashboardQueryService.getDashboardSummary() 최적화 후 성능 측정
// - userNamePort.getUserInfoByUserIds() 3번 → 1번으로 합친 후 측정
// - Railway MySQL 원격 DB 사용 — before 와 동일 조건
//
// 실행:
//   ADMIN_TOKEN=<token> k6 run k6/scripts/07-dashboard-after.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

const ADMIN_TOKEN = __ENV.ADMIN_TOKEN || '';

export const options = {
    vus: 1,
    duration: '120s',
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<10000'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/v1/dashboard/summary`, {
        headers: {
            Authorization: `Bearer ${ADMIN_TOKEN}`,
        },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}

export const handleSummary = createSummaryHandler('07-dashboard-after');
