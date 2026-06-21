// k6/lib/config.js
// ------------------------------------------------------------
// k6 스크립트에서 공통으로 사용하는 설정 모음입니다.
//
// 환경 변수로 테스트 조건을 바꿀 수 있습니다.
// 예시:
//   BASE_URL=http://localhost:8080 k6 run k6/scripts/01-enrollment.js
//   RESULT_NAME=enrollment-before-index k6 run k6/scripts/01-enrollment.js
// ------------------------------------------------------------

// 테스트 대상 서버 주소입니다.
// 환경 변수가 없으면 로컬 기본값을 사용합니다.
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 테스트 결과 파일 이름 prefix입니다.
// 여러 번 실행할 때 RESULT_NAME을 바꾸면 덮어쓰기를 피할 수 있습니다.
export const RESULT_NAME = __ENV.RESULT_NAME || 'k6-result';

// 학생 계정 JWT 토큰 목록입니다. (student1~4)
// 토큰 만료 시 재발급 후 이 배열을 교체합니다.
export const STUDENT_TOKENS = [
    __ENV.TOKEN_STUDENT1 || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicm9sZXMiOiJST0xFX1NUVURFTlQiLCJpYXQiOjE3ODE4Mzg5NDAsImV4cCI6MTc4MTg0MjU0MH0.BUbF3e4ixX7LNxcT0mFZHd8ENhwZeI8IBxLU8QxqxnCEsTzn1q8GZeUWBeOvMJjuk1olnfRcm610jpd3PVz-iA',
    __ENV.TOKEN_STUDENT2 || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzIiwicm9sZXMiOiJST0xFX1NUVURFTlQiLCJpYXQiOjE3ODE4Mzg5NDAsImV4cCI6MTc4MTg0MjU0MH0.WwC1vmZyuKVswk1xL17_NfPe7PA9kepMJEXgLZrEyyKAFAJmmCJTsQLC0M896iPM4eGavAuh1MQh-90uG2uULQ',
    __ENV.TOKEN_STUDENT3 || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0Iiwicm9sZXMiOiJST0xFX1NUVURFTlQiLCJpYXQiOjE3ODE4Mzg5NDEsImV4cCI6MTc4MTg0MjU0MX0.DQfMTrCYiLIiuX9BZbuLJuDdP07qIeRxkDC_BLHSsMcvaVSEIb_qjIui-GCFbgk6VkEndjJzQHQwn0KTE5PCyw',
    __ENV.TOKEN_STUDENT4 || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Iiwicm9sZXMiOiJST0xFX1NUVURFTlQiLCJpYXQiOjE3ODE4Mzg5NDEsImV4cCI6MTc4MTg0MjU0MX0.zELvonyDtt9MuWHwapruOjcFkq56u1iukkdha2nI0mb-nF093b7IevHTe1F7C9z56KQ9dL6EnZJa46oZHP_jvQ',
];

// 현재 DB에 있는 ACTIVE 상태 강의 ID 목록입니다.
export const LECTURE_IDS = [1, 2, 3, 6, 8, 9];

// 강의별 챕터 매핑입니다. (lectureId -> chapterId 배열)
// 진척도 저장 테스트에서 유효한 챕터를 선택할 때 사용합니다.
export const LECTURE_CHAPTER_MAP = {
    1: [1, 2],
    2: [3],
    3: [4],
    6: [6],
    8: [8],
    9: [9],
};

// 요청 사이 사용자 대기 시간 범위입니다.
// 실제 사용자는 API를 쉬지 않고 반복 호출하지 않으므로 sleep이 필요합니다.
export const MIN_SLEEP_SECONDS = Number(__ENV.MIN_SLEEP_SECONDS || 0.5);
export const MAX_SLEEP_SECONDS = Number(__ENV.MAX_SLEEP_SECONDS || 1.5);

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function randomLectureId() {
    return LECTURE_IDS[Math.floor(Math.random() * LECTURE_IDS.length)];
}

export function randomStudentToken() {
    return STUDENT_TOKENS[Math.floor(Math.random() * STUDENT_TOKENS.length)];
}

// VU 번호 기반으로 토큰을 순환 배정합니다.
// 같은 VU는 항상 같은 학생 토큰을 사용해 중복 수강신청 패턴을 재현합니다.
export function vuToken() {
    return STUDENT_TOKENS[__VU % STUDENT_TOKENS.length];
}

export function randomSleepSeconds() {
    return Math.random() * (MAX_SLEEP_SECONDS - MIN_SLEEP_SECONDS) + MIN_SLEEP_SECONDS;
}
