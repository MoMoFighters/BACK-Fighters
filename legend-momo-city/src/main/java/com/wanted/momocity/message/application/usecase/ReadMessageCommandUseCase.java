package com.wanted.momocity.message.application.usecase;

public interface ReadMessageCommandUseCase {
    ReadView handle(Long roomId, Long userId);

    record ReadView(
            Long roomId,
            Long targetUserId,
            String nickname,
            boolean hasUnread //새로 읽은 게 있는지 여부
    ) {}
}
