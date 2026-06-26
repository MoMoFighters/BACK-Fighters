package com.wanted.momocity.store.infrastructure.persistence;

import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.model.Type;
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

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false, unique = true)
    private String name;

    @Column( nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public StoreJpaEntity() {}

    public StoreJpaEntity(Long id, Long price, String url,  Type type,String name, LocalDateTime createdAt) {
        this.id = id;
        this.price = price;
        this.url = url;
        this.type = type;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Store toDomain() {
        return new Store(id, price, url, type, name, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getPrice() {
        return price;
    }

    public Type getType() {
        return type;
    }
}
