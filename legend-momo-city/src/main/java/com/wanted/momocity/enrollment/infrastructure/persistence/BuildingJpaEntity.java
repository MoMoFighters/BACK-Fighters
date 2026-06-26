package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.global.domain.model.Category;
import jakarta.persistence.*;

@Entity
// 유저가 같은 카테고리의 건물 중복 설정 방지를 위한 테이블 설정
@Table(name = "building", uniqueConstraints = { // 매핑할 DB 테이블 이름과 유니크 제약 조건 설정
        @UniqueConstraint(columnNames = {"user_Id", "category"}) // 사용자 별 같은 카테고리 건물 중복 생성 방지
})
public class BuildingJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Long position;

    @Column(nullable = false)
    private Integer level;

    // Building 도메인을 JPA Entity로 변환
    // @Setter를 사용하지 않는 이유는 새 건물 건설에 대한 값이 고정되어 있기 때문에 사용 X
    public static BuildingJpaEntity from(Building building) {
        BuildingJpaEntity entity = new BuildingJpaEntity();
        entity.userId = building.getUserId();
        entity.category = building.getCategory();
        entity.position = building.getPosition();
        entity.level = building.getLevel();

        return entity;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Category getCategory() {
        return category;
    }

    public Long getPosition() {
        return position;
    }

    public Integer getLevel() {
        return level;
    }
}
