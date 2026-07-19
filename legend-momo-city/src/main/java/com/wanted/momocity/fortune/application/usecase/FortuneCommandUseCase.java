package com.wanted.momocity.fortune.application.usecase;

import com.wanted.momocity.fortune.application.result.DrawFortuneResult;

public interface FortuneCommandUseCase {

    // 로그인 사용자의 오늘의 운세를 조회하거나 새로 뽑기
    DrawFortuneResult drawToday(
            Long userId
    );
}
