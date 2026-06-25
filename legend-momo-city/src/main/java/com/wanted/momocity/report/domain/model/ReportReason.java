package com.wanted.momocity.report.domain.model;

// 신고 사유 enum. DB에 영어 문자열로 저장, toKorean()으로 표시용 변환
public enum ReportReason {
    SPAM,
    ABUSE,
    INAPPROPRIATE,
    COPYRIGHT,
    OTHER;

    public String toKorean() {
        return switch (this) {
            case SPAM          -> "스팸/광고";
            case ABUSE         -> "욕설/혐오 표현";
            case INAPPROPRIATE -> "부적절한 내용";
            case COPYRIGHT     -> "저작권 침해";
            case OTHER         -> "기타";
        };
    }
}
