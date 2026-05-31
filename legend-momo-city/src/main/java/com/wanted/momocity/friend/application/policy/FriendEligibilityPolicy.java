package com.wanted.momocity.friend.application.policy;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceConflictException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class FriendEligibilityPolicy {
    //친구 요청이 가능한 상태인지 검증하는 규칙
    public void ensureEligible(Long fromUserId, Long toUserId, Optional<FriendJpaEntity> existingRelation, String targetRole) {

        //자신에게 친구 요청 불가(400)
        if (fromUserId.equals(toUserId)) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 자신에게 친구 요청 시도함 (유저ID: {}", fromUserId);
            throw new FMBusinessRuleViolationException("자기 자신과는 친구 관계를 형성할 수 없습니다.");
        }

        //강사, 관리자에게는 친구 요청 불가(400)
        if (!"STUDENT".equals(targetRole)) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 대상이 강사(TEACHER)임");
            throw new FMBusinessRuleViolationException("일반 수강생 사용자에게만 친구 요청을 보낼 수 있습니다.");
        }

        //기존에 아무런 관계 데이터가 없다면(Optional.isEmpty) -> 첫 요청이므로 무조건 통과
        if (existingRelation.isEmpty()) {
            log.info("[FriendEligibilityPolicy] 이전 관계 내역 없음 - 검증 통과");
            return;
        }

        //기존 관계 데이터가 존재할 때
        FriendJpaEntity relation = existingRelation.get();

        //이미 요청을 보낸 상태인지 확인(409)
        if ("SENT".equals(relation.getStatus())) {
            //로그인 유저가 이미 상대방에게 보낸 경우
            if (relation.getFromUserId().getId().equals(fromUserId)) {
                log.warn("[FriendEligibilityPolicy] 검증 실패 - 이미 요청을 보낸 대상 (현재 상태: SENT)");
                throw new FMResourceConflictException("이미 요청을 보낸 대상입니다.");
            }
            //상대방이 로그인 유저에게 보낸 경우
            if (relation.getToUserId().getId().equals(fromUserId)) {
                log.warn("[FriendEligibilityPolicy] 검증 실패 - 상대방이 로그인 유저에게 이미 요청을 보낸 상태");
                throw new FMResourceConflictException("상대방이 이미 회원님에게 친구 요청을 보낸 상태입니다.");
            }
        }

        //이미 서로 친구 상태인지 확인(추후 확장 대비)
        if ("FRIEND".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 이미 친구 관계인 대상 (현재 상태: FRIEND)");
            throw new FMResourceConflictException("이미 친구 상태인 사용자입니다.");
        }

        //차단 상태인지 확인
        if ("BLOCK".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 차단된 관계 (현재 상태: BLOCK)");
            throw new FMResourceConflictException("차단된 사용자에게는 친구 요청을 보낼 수 없습니다.");
        }
    }

    //친구 요청 수락
    public void ensureAcceptable(Optional<FriendJpaEntity> existingRelation) {
        //수락 전에 철회함(행이 없음)
        if (existingRelation.isEmpty()) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 요청 내역이 존재하지 않음(이미 취소됨)");
            throw new FMResourceConflictException("이미 취소되거나 처리된 요청입니다.");
        }

        FriendJpaEntity relation = existingRelation.get();

        //오직 SENT 상태일 때만 수락 가능(이미 수락되었거나 거절/취소됨)
        if (!"SENT".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 검증 실패 - 대기 상태가 아님 (현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 취소되거나 처리된 요청입니다.");
        }

        log.info("[FriendEligibilityPolicy] 친구 요청 수락 자격 검증 완료");
    }

    //친구 요청 거절 검증
    public void ensureRejectable(Optional<FriendJpaEntity> existingRelation) {
        //거절 전에 철회(404)
        if (existingRelation.isEmpty()) {
            log.warn("[FriendEligibilityPolicy] 거절 검증 실패 - 요청 내역이 존재하지 않음");
            throw new FMResourceNotFoundException("거절할 친구 요청 내역이 존재하지 않습니다.");
        }

        FriendJpaEntity relation = existingRelation.get();

        //오직 SENT일 때만 거절 가능(이미 친구 or 차단이면 에러)(409)
        if (!"SENT".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 거절 검증 실패 - 대기 상대가 아님 (현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 취소되거나 처리된 요청입니다.");
        }

        log.info("[FriendEligibilityPolicy] 친구 요청 거절 자격 검증 완료");
    }

    //차단 검증
    public void ensureBlockable(Optional<FriendJpaEntity> existingRelation, String targetRole) {

        //강사는 차단 불가(409)
        if ("TEACHER".equals(targetRole)) {
            log.warn("[FriendEligibilityPolicy] 차단 검증 실패 - 대상이 강사(TEACHER)임");
            throw new FMResourceConflictException("강사는 차단할 수 없습니다.");

        }
        //친구 행 없으면 아니면 차단 불가(409)
        if (existingRelation.isEmpty()) {
            log.warn("[FriendEligibilityPolicy] 차단 검증 실패 - 아무런 관계 내역이 없음(친구 상태가 아님)");
            throw new FMResourceConflictException("이미 차단되었거나 차단할 수 없는 대상입니다.");
        }

        FriendJpaEntity relation = existingRelation.get();

        //친구 상태(FRIEND)일 때만 차단 가능(409)
        if (!"FRIEND".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 차단 검증 실패 - 친구 상태가 아님 (현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 차단되었거나 차단할 수 없는 대상입니다.");
        }

        log.info("[FriendEligibilityPolicy] 친구 차단 자격 검증 완료 (현재 친구 상태 확인됨)");
    }

    //차단 해제 검증
    public void ensureUnblockable(Optional<FriendJpaEntity> existingRelation) {
        //관계 행 자체가 존재하는지 확인(404)
        if (existingRelation.isEmpty()) {
            log.warn("[FriendEligibilityPolicy] 차단 해제 검증 실패 - 관계 내역이 존재하지 않음");
            throw new FMResourceNotFoundException("차단 해제할 친구 내역이 존재하지 않습니다.");
        }

        FriendJpaEntity relation = existingRelation.get();

        //BLOCK일 때만 차단 해제 가능(409)
        if (!"BLOCK".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 차단 해제 검증 실패 - 차단 상태가 아님 (현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 친구 관계가 아니거나 차단 해제할 수 없는 대상입니다.");
        }

        log.info("[FriendEligibilityPolicy] 친구 차단 해제 자격 검증 완료 (차단 상태 확인됨)");
    }

    //친구 삭제 검증
    public void ensureDeletable(Optional<FriendJpaEntity> existingRelation, String targetRole) {
        //관계 행 자체가 없는지 확인(404)
        if (existingRelation.isEmpty()) {
            log.warn("[FriendEligibilityPolicy] 친구 삭제 검증 실패 - 관계 내역이 존재하지 않음");
            throw new FMResourceNotFoundException("삭제할 친구 내역이 존재하지 않습니다.");
        }

        //강사는 수강이 유지되는 한 임의로 삭제 불가(409)
        if ("TEACHER".equals(targetRole)) {
            log.warn("[FriendEligibilityPolicy] 친구 삭제 검증 실패 - 대상이 강사임");
            throw new FMResourceConflictException("이미 친구 관계가 아니거나 삭제할 수 없는 대상입니다.");
        }

        FriendJpaEntity relation = existingRelation.get();

        //FRIEND 상태가 아닐 때 삭제 불가(409)
        if (!"FRIEND".equals(relation.getStatus())
        && !"BLOCK".equals(relation.getStatus())) {
            log.warn("[FriendEligibilityPolicy] 친구 삭제 검증 실패 - 친구 상태가 아님(현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 친구 관계가 아니거나 삭제할 수 없는 대상입니다.");
        }

        log.info("[FriendEligibilityPolicy] 친구 삭제 자격 검증 완료 (정상 친구 상태 확인됨)");
    }
}
