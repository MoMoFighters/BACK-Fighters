package com.wanted.momocity.friend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataGuestBookRepository  extends JpaRepository<GuestBookJpaEntity, Long> {

    //방명록 목록 조회
    @Query("SELECT g FROM GuestBookJpaEntity g " +
            "JOIN FETCH g.writerId " +
            "WHERE g.ownerId.id = :ownerId " +
            "ORDER BY g.createdAt DESC")
    List<GuestBookJpaEntity> findAllByOwnerIdWithWriter(@Param("ownerId") Long ownerId);

    //방명록 작성 1일1제한
    @Query("SELECT COUNT(g) > 0 FROM GuestBookJpaEntity g " +
            "WHERE g.writerId.id = :userId " +
            "AND g.ownerId.id = :ownerId " +
            "AND DATE(g.createdAt) = :today")
    boolean existsWrittenToday(
            @Param("userId") Long userId,
            @Param("ownerId") Long ownerId,
            @Param("today") LocalDate today
    );

    //내가 쓴 방명록 목록 조회
    //내가 남의 도시에 남긴 방명록 목록 조회
    @Query("SELECT g FROM GuestBookJpaEntity g " +
            "JOIN FETCH g.writerId " +
            "WHERE g.writerId.id = :writerId AND g.ownerId.id = :ownerId " +
            "ORDER BY g.createdAt DESC")
    List<GuestBookJpaEntity> findAllByWriterIdAndOwnerIdWithWriter(@Param("writerId") Long writerId,
                                                                   @Param("ownerId") Long ownerId);
}
