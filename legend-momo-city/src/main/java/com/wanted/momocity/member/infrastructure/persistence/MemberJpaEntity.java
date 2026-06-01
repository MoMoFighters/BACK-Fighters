package com.wanted.momocity.member.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* comment.
    MemberJpaEntity 정리
    1. 해당 클래스의 역할 : user 테이블과 1:1 매핑되는 JPA 저장 모델
    2. 회원 영역이 단독적으로 소유 : 강사/신고/관리자영역은 이 클래스에 직접적으로 매핑 절대 금지!!
    3. Member 도메인 모델과의 관계
        - 같은 회원을 표현하지만 완전히 별개 클래스
        - 변환은 MemberRepositoryAdapter 가 책임지게 된다.
 */

@Entity // JPA 가 이 클래스를 영속성 객체로 인식하게 함
@Table(name = "user") // " " 테이블과 매핑 명시
public class MemberJpaEntity extends BaseTimeEntity {

    // ENUM 매핑이 String 인 이유 : role/status/category 가 도메인에서 ENUM 인데 여기서는
    // STRING -> Adapter 가 enum.name() <-> String  변환

    // @Id + @GeneratedValue : DB auto increment 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "category")
    private String category;

    @Column(name = "proof", length = 500)
    private String proof;

    @Column(name = "point")
    private Integer point;

    @Column(name = "is_paid")
    private Boolean isPaid;

    @Column(name = "do_not_disturb")
    private Boolean doNotDisturb;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_tempPWD")
    private Boolean isTempPWD;

    protected MemberJpaEntity() {
    }

    // JPA 저장 모델 메소드 (도메인 행위 아님). 도메인 검증은 Member.approveAsTeacher() 등이 담당.
    public void changeRole(String role) {
        this.role = role;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    // 현재 쓰지 않는 변수들도 있지만, 추후 확장 가능성을 위해서 추가해놓은 상태
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public LocalDate getBirth() { return birth; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getProof() { return proof; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
