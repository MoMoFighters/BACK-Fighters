package com.wanted.momocity.notification.application.service;

import com.wanted.momocity.notification.application.usecase.NotificationCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationCommandService implements NotificationCommandUseCase {
}
