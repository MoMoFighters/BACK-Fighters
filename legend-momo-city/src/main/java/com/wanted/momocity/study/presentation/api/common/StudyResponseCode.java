package com.wanted.momocity.study.presentation.api.common;

/*
 * comment.
 *  Study(열품타) 컨텍스트 전용 응답 코드 모음
 *  - 성공 코드만 관리 (실패 코드는 global ApiResponseCode.DOMAIN_RULE_VIOLATION 등을 그대로 사용)
 *  - ApiExceptionHandler(global)가 DomainRuleViolationException을 COMMON-DOMAIN-RULE-VIOLATION
 *    하나로 처리하므로, 케이스별 실패 코드는 별도로 두지 않고 message로 구분
 */
public class StudyResponseCode {

    private StudyResponseCode() {
    }

    // ===== room =====
    public static final String ROOM_CREATED       = "STUDY-ROOM-CREATED";
    public static final String ROOM_FETCHED       = "STUDY-ROOM-FETCHED";
    public static final String ROOM_LIST_FETCHED  = "STUDY-ROOM-LIST-FETCHED";
    public static final String ROOM_UPDATED       = "STUDY-ROOM-UPDATED";

    // ===== member : invitation =====
    public static final String INVITATION_SENT        = "STUDY-INVITATION-SENT";
    public static final String INVITATION_CANCELED    = "STUDY-INVITATION-CANCELED";
    public static final String INVITATION_ACCEPTED    = "STUDY-INVITATION-ACCEPTED";
    public static final String INVITATION_REJECTED    = "STUDY-INVITATION-REJECTED";
    public static final String INVITATION_LIST_FETCHED = "STUDY-INVITATION-LIST-FETCHED";

    // ===== member : timer =====
    public static final String TIMER_STARTED  = "STUDY-TIMER-STARTED";  // action=STARTED/RESUMED 로 세부 구분
    public static final String TIMER_PAUSED   = "STUDY-TIMER-PAUSED";
    public static final String TIMER_ENDED    = "STUDY-TIMER-ENDED";
    public static final String TIMER_LAPS_FETCHED = "STUDY-TIMER-LAPS-FETCHED";

    // ===== member : leave / kick =====
    public static final String MEMBER_LEFT    = "STUDY-MEMBER-LEFT";
    public static final String MEMBER_KICKED  = "STUDY-MEMBER-KICKED";

    // ===== solo =====
    public static final String SOLO_STARTED        = "STUDY-SOLO-STARTED"; // action 필드로 STARTED/RESUMED 구분
    public static final String SOLO_PAUSED         = "STUDY-SOLO-PAUSED";
    public static final String SOLO_ENDED          = "STUDY-SOLO-ENDED";
    public static final String SOLO_CURRENT_FETCHED = "STUDY-SOLO-CURRENT-FETCHED";
    public static final String SOLO_CURRENT_EMPTY   = "STUDY-SOLO-CURRENT-EMPTY";
    public static final String SOLO_LAPS_FETCHED    = "STUDY-SOLO-LAPS-FETCHED";

    // ===== record =====
    public static final String RECORD_DAILY_FETCHED    = "STUDY-RECORD-DAILY-FETCHED";
    public static final String RECORD_MONTHLY_FETCHED  = "STUDY-RECORD-MONTHLY-FETCHED";
    public static final String RECORD_YEARLY_FETCHED   = "STUDY-RECORD-YEARLY-FETCHED";
    public static final String RANKING_DAILY_FETCHED   = "STUDY-RANKING-DAILY-FETCHED";
    public static final String RANKING_MONTHLY_FETCHED = "STUDY-RANKING-MONTHLY-FETCHED";
}