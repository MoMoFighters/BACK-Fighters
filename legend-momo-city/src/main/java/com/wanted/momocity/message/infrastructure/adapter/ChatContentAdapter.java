package com.wanted.momocity.message.infrastructure.adapter;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.message.infrastructure.persistence.SpringDataMessageRepository;
import com.wanted.momocity.report.application.port.ChatContentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatContentAdapter implements ChatContentPort {

    private final SpringDataMessageRepository springDataMessageRepository;

    @Override
    public String getContentById(Long chatId) {

        return springDataMessageRepository.findById(chatId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 메시지입니다."))
                .getContent();
    }
}
