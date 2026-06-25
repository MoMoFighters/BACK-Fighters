package com.wanted.momocity.order.application.service;

import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements OrderQueryUsecase {
}
