package com.wanted.momocity.store.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Store {

    private final Long id;
    private final Long price;
    private final String url;
    private final Type type;
    private final String name;
    private final LocalDateTime createdAt;

    public Store(Long id, Long price, String url, Type type, String name, LocalDateTime createdAt) {
        this.id = id;
        this.price = price;
        this.url = url;
        this.type = type;
        this.name = name;
        this.createdAt = createdAt;
    }


}
