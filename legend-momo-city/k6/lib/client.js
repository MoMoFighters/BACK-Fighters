// k6/lib/client.js
// ------------------------------------------------------------
// MoMo City API 호출을 함수로 감싼 파일입니다.
//
// 의도:
// - 시나리오 스크립트는 "사용자 행동"에만 집중합니다.
// - HTTP 세부 구현(헤더, URL, check)은 이 파일에서 관리합니다.
// - tags를 일관되게 붙여 k6 결과에서 API별 지표를 분리합니다.
//   예: threshold에서 'http_req_duration{type:enrollment}'처럼 사용할 수 있습니다.
// ------------------------------------------------------------

import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, randomLectureId, LECTURE_CHAPTER_MAP, EXPIRED_TOKEN } from './config.js';

function authHeaders(token) {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
}

// 강의 목록 조회 API입니다.
// category, keyword, page는 선택값입니다.
export function getLectures(params = {}, token = null) {
    const { category, keyword, page = 1, size = 10 } = params;

    let url = `${BASE_URL}/api/v1/lectures?page=${page}&size=${size}`;
    if (category) url += `&category=${category}`;
    if (keyword)  url += `&keyword=${encodeURIComponent(keyword)}`;

    const headers = token
        ? authHeaders(token)
        : { 'Content-Type': 'application/json' };

    const res = http.get(url, {
        headers,
        tags: { type: 'lecture-list', api: 'GET /api/v1/lectures' },
    });

    check(res, {
        'lecture-list: status is 200': (r) => r.status === 200,
        'lecture-list: no server error':  (r) => r.status !== 500,
    });

    return res;
}

// 수강신청 API입니다.
// 첫 번째 성공은 201, 이미 신청한 경우 409 또는 400을 반환합니다.
// responseCallback으로 400/409를 기대된 응답으로 등록해
// k6의 http_req_failed 메트릭에서 실패로 집계되지 않도록 합니다.
export function enrollLecture(token, lectureId = randomLectureId()) {
    const res = http.post(
        `${BASE_URL}/api/v1/lectures/${lectureId}/enrollments`,
        null,
        {
            headers: authHeaders(token),
            tags: { type: 'enrollment', api: 'POST /api/v1/lectures/{id}/enrollments' },
            responseCallback: http.expectedStatuses(201, 400, 409),
        }
    );

    check(res, {
        'enrollment: success or duplicate': (r) => r.status === 201 || r.status === 409 || r.status === 400,
        'enrollment: no server error':      (r) => r.status !== 500,
    });

    return res;
}

// 진척도 저장 API입니다.
// 프론트에서 5~10초 주기로 현재 재생 위치를 전송하는 패턴을 재현합니다.
// 90% 이상 시청 시 서버에서 챕터 완료 처리를 수행합니다.
export function saveProgress(token, lectureId, chapterId, playbackSeconds) {
    const res = http.patch(
        `${BASE_URL}/api/v1/lectures/${lectureId}/chapters/${chapterId}/progress`,
        JSON.stringify({ playbackSeconds }),
        {
            headers: authHeaders(token),
            tags: { type: 'progress', api: 'PATCH /api/v1/lectures/{id}/chapters/{id}/progress' },
        }
    );

    check(res, {
        'progress: status is 200':  (r) => r.status === 200,
        'progress: no server error': (r) => r.status !== 500,
    });

    return res;
}

// 강의 목록 조회 + 수강신청을 묶은 일반 학생 행동 시나리오입니다.
// 혼합 트래픽 테스트에서 사용합니다.
export function studentBrowseAndEnroll(token) {
    getLectures({}, token);
    enrollLecture(token, randomLectureId());
}

// 만료된 토큰으로 진척도 저장 API를 호출합니다.
// 서버가 401을 안정적으로 반환하는지 확인하는 시나리오(05-expired-token.js)에서 사용합니다.
export function requestWithExpiredToken(lectureId, chapterId, playbackSeconds) {
    const res = http.patch(
        `${BASE_URL}/api/v1/lectures/${lectureId}/chapters/${chapterId}/progress`,
        JSON.stringify({ playbackSeconds }),
        {
            headers: authHeaders(EXPIRED_TOKEN),
            tags: { type: 'expired-token', api: 'PATCH /api/v1/lectures/{id}/chapters/{id}/progress' },
            responseCallback: http.expectedStatuses(401),
        }
    );

    check(res, {
        'expired-token: status is 401':   (r) => r.status === 401,
        'expired-token: no server error': (r) => r.status !== 500,
    });

    return res;
}