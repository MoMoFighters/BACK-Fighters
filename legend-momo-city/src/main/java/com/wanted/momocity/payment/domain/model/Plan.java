package com.wanted.momocity.payment.domain.model;

public enum Plan {
    // 구독 플랜 종류
    BASIC(0L),
    PLUS(29900L),
    PRO(49900L);

    private final Long price;

    Plan(Long price) {
        this.price = price;
    }

    public Long getPrice() {
        return price;
    }

    public boolean isUpgradeFrom(Plan current) {
        return this.price > current.price;
    }

    public boolean isDowngradeFrom(Plan current) {
        return this.price < current.price;
    }
}
