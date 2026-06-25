package com.wanted.momocity.store.infrastructure.persistence;

import com.wanted.momocity.store.domain.model.Store;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="store")
public class StoreJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long price;

    @Column
    private String url;

    @Column(nullable = false)
    private String name;

    @Column( nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public StoreJpaEntity() {}

    public StoreJpaEntity(Long id, Long price, String url, String name, LocalDateTime createdAt) {
        this.id = id;
        this.price = price;
        this.url = url;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Store toDomain() {
        return new Store(id, price, url, name, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getPrice() {
        return price;
    }
}
