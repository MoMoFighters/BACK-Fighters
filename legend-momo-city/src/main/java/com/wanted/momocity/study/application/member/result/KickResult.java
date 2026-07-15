package com.wanted.momocity.study.application.member.result;

/*
 * comment.
 *  강퇴 결과 DTO (KickResponse로 매핑)
 *  - status는 항상 "KICKED" 고정값
 * */

public record KickResult(
        Long roomId,
        Long targetUserId,
        String status
) {

    public static KickResult of(Long roomId, Long targetUserId) {
        return new KickResult(roomId, targetUserId, "KICKED");
    }
}