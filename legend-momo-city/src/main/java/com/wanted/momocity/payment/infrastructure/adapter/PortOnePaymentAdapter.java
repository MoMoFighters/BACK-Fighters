package com.wanted.momocity.payment.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PortOnePaymentAdapter {
    private final WebClient portOneWebClient;

}
