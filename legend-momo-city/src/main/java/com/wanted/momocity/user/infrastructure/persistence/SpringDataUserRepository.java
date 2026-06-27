package com.wanted.momocity.user.infrastructure.persistence;


import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.nickname = :nickname WHERE u.id = :userId")
    void registerNickname(@Param("userId") Long userId, @Param("nickname") String nickname);

    boolean existsByNickname(String nickname);

    // 값이 있으면 바꾸고 널이면 기본 값 유지
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET " +
            "u.nickname = CASE WHEN :nickname IS NOT NULL THEN :nickname ELSE u.nickname END, " +
            "u.profileImageUrl = CASE WHEN :profileImageUrl IS NOT NULL THEN :profileImageUrl ELSE u.profileImageUrl END, " +
            "u.password = CASE WHEN :password IS NOT NULL THEN :password ELSE u.password END " +
            "WHERE u.id = :userId")
    void updateUserInfo(@Param("userId") Long userId,
                        @Param("nickname") String nickname,
                        @Param("profileImageUrl") String profileImageUrl,
                        @Param("password") String password);


    @Query("SELECT u FROM UserUser u WHERE u.role = :role AND u.status = :status ORDER BY u.updatedAt DESC")
    List<UserJpaEntity> findByRoleAndStatus(@Param("role") Role role, @Param("status") Status status, Pageable pageable);

    long countByRoleAndStatus(Role role, Status status);


    // 강사 신청
    // role : STUDENT -> TEACHER
    // status : ACTIVE -> PENDING
    // proof : null -> S3 url
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.role = 'TEACHER', u.status = 'PENDING', u.category = :category, u.proof = :proof, u.nickname = :nickname " +
            "WHERE u.id = :userId")
    void teacherApply(@Param("userId") Long userId,
                      @Param("nickname") String nickname,
                      @Param("category") Category category,
                      @Param("proof") String proof);

    // 강사 중복 신청 방지용
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserUser u WHERE u.id = :userId AND u.role = :role AND u.status IN :status")
    boolean checkTeacherAvailable(@Param("userId") Long userId,
                                  @Param("role") Role role,
                                  @Param("status") List<Status> status);

    // 강사 승인 여부에 따른 status 변환
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.role = :role, u.status = :status, " +
            "u.profileImageUrl = CASE WHEN :profileImageUrl IS NOT NULL THEN :profileImageUrl ELSE u.profileImageUrl END, " +
            "u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updateRoleAndStatus(@Param("userId") Long userId,
                             @Param("role") Role role,
                             @Param("status") Status status,
                             @Param("profileImageUrl") String profileImageUrl,
                             @Param("updatedAt") LocalDateTime updatedAt);

    // 임시비번을 사용자가 변경했을 때
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.isTempPwd = false WHERE u.id = :userId")
    void clearTempPwd(@Param("userId") Long userId);

    // active인 사용자 수 세기
    long countByStatus(Status status);

    // 특정 날짜 이전의 active인 사용자 수 세기
    long countByStatusAndCreatedAtBefore(Status status, LocalDateTime localDateTime);

    // 관리자 회원 목록 조회용
    /*
     * status = DELETED  → 탈퇴회원만 조회
     * status = null     → 탈퇴회원 제외 전체 조회
     *                     role = null  이면 전체 (STUDENT + TEACHER)
     *                     role = 값 있으면 해당 role만
     * */
    @Query("SELECT u FROM UserUser u WHERE " +
            "u.role <> 'ADMIN' AND u.status <> 'REJECTED' AND (" +
            "(:status = 'DELETED' AND u.status = :status) OR " +
            "(:status IS NULL AND u.status <> 'DELETED' AND (:role IS NULL OR u.role = :role))) " +
            "ORDER BY u.createdAt DESC")
    List<UserJpaEntity> findAllForAdmin(
            @Param("role") Role role,
            @Param("status") Status status,
            Pageable pageable
    );

    // 관리자 회원 목록 전체 개수 조회 (페이지네이션 totalElements 계산용)
    @Query("SELECT COUNT(u) FROM UserUser u WHERE " +
            "u.role <> 'ADMIN' AND u.status <> 'REJECTED' AND (" +
            "(:status = 'DELETED' AND u.status = :status) OR " +
            "(:status IS NULL AND u.status <> 'DELETED' AND (:role IS NULL OR u.role = :role))) " +
            "ORDER BY u.createdAt DESC")
    long countForAdmin(
            @Param("role") Role role,
            @Param("status") Status status
    );

    // 밤티 알림 설정
    // 기존에 true 이면 false로
    // 기존에 false면 true로
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserUser u SET u.doNotDisturb = CASE " +
            "WHEN u.doNotDisturb = true " +
            "THEN false " +
            "ELSE true END WHERE u.id = :userId")
    @Transactional
    void setAlarm(@Param("userId") Long userId);

    // 강사 포기
    // 기존에 강사 신청했다가 반려된 사람 = REJECTED + STUDENT 이
    // 더이상 강사 신청을 안 할거라면 status를 ACTIVE로 바꿔줌
    @Query("UPDATE UserUser u SET u.status = :status WHERE u.id = :userId")
    @Modifying
    @Transactional
    void changeStatus(
            @Param("userId") Long userId,
            @Param("status") Status status);

    // 승인할 강사의 카테고리 가져오기
    @Query("SELECT u.category FROM UserUser u WHERE u.id = :userId")
    Category findCategoryById(@Param("userId") Long userId);

    // 회원탈퇴 (소프트 딜리트)
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.status = :status, u.nickname = :nickname, u.deletedAt = :deletedAt WHERE u.id = :userId")
    void changeStatusAndNickname(
            @Param("userId") Long userId,
            @Param("status") Status status,
            @Param("nickname") String nickname,
            @Param("deletedAt") LocalDateTime deletedAt
    );

    // 사용자 하드 딜리트
    @Query("SELECT u.id FROM UserUser u WHERE u.status = 'DELETED' AND u.deletedAt < :threshold")
    List<Long> findDeletedUserIdsBefore(@Param("threshold") LocalDateTime threshold);

    // 사용자 신고 횟수 +
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.suspensionCount = COALESCE(u.suspensionCount, 0) + 1 WHERE u.id = :userId")
    void plusReportCount(@Param("userId") Long userId);

    // 변화한 신고 카운트 조회 -> status, 정지기간 설정용
    @Query("SELECT u.suspensionCount FROM UserUser u WHERE u.id = :userId")
    Long findSuspensionCountById(@Param("userId") Long userId);

    // 신고 횟수에 따라 정지 적용
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.status = :status, u.suspendedUntil = :suspendedUntil WHERE u.id = :userId")
    void reportApply(@Param("userId") Long userId,
                          @Param("status") Status status,
                          @Param("suspendedUntil") LocalDateTime suspendedUntil);

    // 정지 풀어주기
    // banned -> active
    // 정지기간 -> null
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.status = 'ACTIVE', u.suspendedUntil = null WHERE u.status = 'BANNED' AND u.suspendedUntil <= :now")
    void banOver(@Param("now") LocalDateTime now);

    // 사용자 신고 횟수 -
    // status -> active
    // suspensionCount -1
    // suspendedUntil -> null
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.suspensionCount = u.suspensionCount - 1, u.status = 'ACTIVE', u.suspendedUntil = null WHERE u.id = :userId")
    void minusReportCount(@Param("userId") Long userId);

}
