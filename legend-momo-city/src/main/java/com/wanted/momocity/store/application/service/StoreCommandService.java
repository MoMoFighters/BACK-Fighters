package com.wanted.momocity.store.application.service;

import com.wanted.momocity.store.application.usecase.StoreCommandUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StoreCommandService implements StoreCommandUsecase {
}
