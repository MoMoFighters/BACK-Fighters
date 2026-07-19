package com.wanted.momocity.fortune.infrastructure.persistence;

import com.wanted.momocity.fortune.domain.model.Fortune;
import com.wanted.momocity.fortune.domain.model.FortuneTone;
import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fortunes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FortuneJpaEntity extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @Enumerated(EnumType.STRING)

    @Column(name = "tone", nullable = false)
    private FortuneTone tone;

    public Fortune toDomain() {

        return Fortune.reconstitute(
                id,
                content,
                tone,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
