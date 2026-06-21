package com.wanted.momocity.streak.domain.model;

/*
* comment.
*  잔디 레벨 정책 ENUM
*  -> daily_watched_seconds 기준으로 레벨 결정
* */

public enum StreakLevel {

    LEVEL0, LEVEL1, LEVEL2, LEVEL3, LEVEL4;

    public static StreakLevel from(int seconds) {
        if (seconds <= 0)    return LEVEL0;
        if (seconds <= 600)  return LEVEL1;
        if (seconds <= 1800) return LEVEL2;
        if (seconds <= 3600) return LEVEL3;
        return LEVEL4;
    }

}
