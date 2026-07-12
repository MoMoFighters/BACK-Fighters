package com.wanted.momocity.payment.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class PortOnePaymentAdapter {
    private final WebClient portOneWebClient;

}
