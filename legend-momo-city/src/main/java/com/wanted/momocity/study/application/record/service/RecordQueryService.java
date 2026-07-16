package com.wanted.momocity.study.application.record.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.record.usecase.RecordQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.DailyStudyRecord;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.model.MonthlyStudyRecord;
import com.wanted.momocity.study.domain.repository.DailyStudyRecordRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.domain.repository.MonthlyStudyRecordRepository;
import com.wanted.momocity.study.presentation.api.response.record.DailyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.MonthlyRecordResponse;
import com.wanted.momocity.study.presentation.api.response.record.RankingResponse;
import com.wanted.momocity.study.presentation.api.response.record.YearlyRecordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * comment.
 *  개인 공부 기록 통계 읽기 작업 UseCase 구현체
 *  -
 *  daily/monthly/yearly는 로그인한 본인의 기록만 조회하므로 별도 권한 검증이 필요 없고,
 *  랭킹 2개(daily/monthly)는 "같은 방 참가자만 조회 가능" 검증이 필요
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecordQueryService implements RecordQueryUseCase {

    private final DailyStudyRecordRepository dailyStudyRecordRepository;
    private final MonthlyStudyRecordRepository monthlyStudyRecordRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final StudyUserInfoPort studyUserInfoPort;

    // 특정 날짜 개인 누적시간 조회 - 기록이 없는 날짜는 404가 아니라 0으로 정상 응답
    @Override
    public DailyRecordResponse getDaily(Long userId, LocalDate date) {
        int totalSeconds = dailyStudyRecordRepository.findByUserIdAndStudyDate(userId, date)
                .map(DailyStudyRecord::getTotalSeconds)
                .orElse(0);

        log.info("[Study] 일별 공부시간 조회 완료 | userId={}, date={}", userId, date);
        return new DailyRecordResponse(date, totalSeconds);
    }

    // 특정 월 개인 누적시간 조회 - 기록이 없는 월도 0으로 정상 응답
    @Override
    public MonthlyRecordResponse getMonthly(Long userId, YearMonth yearMonth) {
        int totalSeconds = monthlyStudyRecordRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .map(MonthlyStudyRecord::getTotalSeconds)
                .orElse(0);

        log.info("[Study] 월별 공부시간 조회 완료 | userId={}, yearMonth={}", userId, yearMonth);
        return new MonthlyRecordResponse(yearMonth.toString(), totalSeconds);
    }

    // 연간 잔디 조회 - 쿼리스트링 없이 로그인 유저 기준 올해 1년치 반환
    @Override
    public YearlyRecordResponse getYearly(Long userId) {
        int currentYear = LocalDate.now().getYear();
        var records = dailyStudyRecordRepository.findAllByUserIdAndYear(userId, currentYear);

        var items = records.stream()
                .map(r -> new YearlyRecordResponse.DayRecord(r.getStudyDate(), r.getTotalSeconds()))
                .toList();

        log.info("[Study] 연간 잔디 조회 완료 | userId={}, year={}, count={}", userId, currentYear, items.size());
        return new YearlyRecordResponse(items);
    }

    // 그룹방 멤버 일별 랭킹 조회
    @Override
    public RankingResponse getDailyRanking(Long userId, Long roomId) {
        var members = getValidatedRoomMembers(userId, roomId);
        LocalDate today = LocalDate.now();

        List<Long> userIds = members.stream().map(GroupRoomMember::getUserId).toList();
        var records = dailyStudyRecordRepository.findAllByUserIdsAndStudyDate(userIds, today);
        Map<Long, Integer> secondsByUser = records.stream()
                .collect(Collectors.toMap(DailyStudyRecord::getUserId, DailyStudyRecord::getTotalSeconds));

        var ranking = buildRankedItems(userIds, secondsByUser);

        log.info("[Study] 방 일별 랭킹 조회 완료 | roomId={}, date={}", roomId, today);
        return new RankingResponse(today.toString(), ranking);
    }

    // 그룹방 멤버 월별 랭킹 조회
    @Override
    public RankingResponse getMonthlyRanking(Long userId, Long roomId) {
        var members = getValidatedRoomMembers(userId, roomId);
        YearMonth thisMonth = YearMonth.now();

        List<Long> userIds = members.stream().map(GroupRoomMember::getUserId).toList();
        var records = monthlyStudyRecordRepository.findAllByUserIdsAndYearMonth(userIds, thisMonth);
        Map<Long, Integer> secondsByUser = records.stream()
                .collect(Collectors.toMap(MonthlyStudyRecord::getUserId, MonthlyStudyRecord::getTotalSeconds));

        var ranking = buildRankedItems(userIds, secondsByUser);

        log.info("[Study] 방 월별 랭킹 조회 완료 | roomId={}, yearMonth={}", roomId, thisMonth);
        return new RankingResponse(thisMonth.toString(), ranking);
    }

    // ===== 내부 헬퍼 =====

    // 요청자가 방 참가자인지 검증 후, 현재 참가자(JOINED) 목록 반환
    private List<GroupRoomMember> getValidatedRoomMembers(Long userId, Long roomId) {
        groupRoomRepository.findByIdAndActive(roomId)
                .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));

        var members = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId);
        boolean requesterIsMember = members.stream().anyMatch(m -> m.getUserId().equals(userId));
        if (!requesterIsMember) {
            throw new StudyAccessDeniedException("그룹방 참가자만 조회할 수 있습니다.");
        }
        return members;
    }

    // 정렬 + 순위 매기기를 명확하게 분리한 최종 구현
    private List<RankingResponse.RankingItem> buildRankedItems(List<Long> userIds, Map<Long, Integer> secondsByUser) {
        var sorted = userIds.stream()
                .sorted(Comparator.comparingInt((Long uid) -> secondsByUser.getOrDefault(uid, 0)).reversed())
                .toList();

        return java.util.stream.IntStream.range(0, sorted.size())
                .mapToObj(i -> {
                    Long uid = sorted.get(i);
                    User user = studyUserInfoPort.findById(uid)
                            .orElseThrow(() -> new StudyNotFoundException("사용자를 찾을 수 없습니다."));
                    return new RankingResponse.RankingItem(
                            i + 1, uid, user.getNickname(), secondsByUser.getOrDefault(uid, 0)
                    );
                })
                .toList();
    }


}
