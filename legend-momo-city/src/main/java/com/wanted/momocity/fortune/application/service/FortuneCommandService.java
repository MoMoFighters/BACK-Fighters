package com.wanted.momocity.fortune.application.service;

import com.wanted.momocity.fortune.application.port.FortunePointPort;
import com.wanted.momocity.fortune.application.result.DrawFortuneResult;
import com.wanted.momocity.fortune.application.usecase.FortuneCommandUseCase;
import com.wanted.momocity.fortune.domain.exception.FortuneNotFoundException;
import com.wanted.momocity.fortune.domain.exception.InsufficientFortunePointException;
import com.wanted.momocity.fortune.domain.model.Fortune;
import com.wanted.momocity.fortune.domain.model.UserFortuneLog;
import com.wanted.momocity.fortune.domain.repository.FortuneRepository;
import com.wanted.momocity.fortune.domain.repository.UserFortuneLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class FortuneCommandService implements FortuneCommandUseCase {

    // 뽑기할 때 5포인트 차감
    private static final Long FORTUNE_DRAW_COST = 5L;
    // 운세 날짜를 계산할 대한민국 시간대
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final FortuneRepository fortuneRepository;
    private final UserFortuneLogRepository userFortuneLogRepository;
    private final FortunePointPort fortunePointPort;

    // 로그인 한 사용자의 오늘의 운세를 조회하거나 새로 뽑기
    @Override
    public DrawFortuneResult drawToday(Long userId) {

        // 서버 기본 시간대 상관없이 한국 시간대로 계산
        LocalDate today = LocalDate.now(KST);

        // 이미 뽑은 운세가 있는지 조회
        UserFortuneLog existingLog = userFortuneLogRepository
                .findByUserIdAndDrawnDate(userId, today)
                .orElse(null);

        // 오늘 봅은 운세가 있다면 포인트 차감 X
        if (existingLog !=null) {
            // 기존 기록에 저장된 운세 ID로 운세를 조회
            Fortune existingFortune = fortuneRepository
                    .findById(existingLog.getFortuneId())

                    // 운세 데이터가 없으면 데이터 정합성 예외 발생
                    .orElseThrow(FortuneNotFoundException::new);

            // 기존에 뽑은 운세 그대로 반환
            return DrawFortuneResult.from(existingFortune, today);
        }

        // 오늘 처음 요청한 경우 366개 중 무작위 운세 한 건을 조회
        Fortune selectedFortune = fortuneRepository.findRandom().orElseThrow(FortuneNotFoundException::new);

        //사용자 5포인트 이상 보유한 경우에 포인트 차감
        boolean deducted = fortunePointPort.deductPointIfEnough(userId, FORTUNE_DRAW_COST);

        // 포인트가 부족하면 예외 발생
        if(!deducted) {
            throw new InsufficientFortunePointException();
        }

        // 오늘 선택 된 운세를 사용자의 날짜별 운세 기록을 생성
        UserFortuneLog newLog = UserFortuneLog.create(
                userId,
                selectedFortune.getId(),
                today
        );

        // 같은 날 다시 요청할 때 같은 운세 반환할 수 있도록 저장
        userFortuneLogRepository.save(newLog);

        return DrawFortuneResult.from(selectedFortune, today);
    }
}
