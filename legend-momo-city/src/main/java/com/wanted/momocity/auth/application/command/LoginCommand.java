package com.wanted.momocity.auth.application.command;

public record LoginCommand(
        String email,
        String password,
        // [MS-4 접근로그] admin BC 접근로그 저장을 위해 ip 필드 추가 (auth BC 담당자 승인, 예외적 BC 간 수정)
        String ip
) {
}
