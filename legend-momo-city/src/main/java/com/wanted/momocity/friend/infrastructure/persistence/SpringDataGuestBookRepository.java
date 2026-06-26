package com.wanted.momocity.friend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataGuestBookRepository  extends JpaRepository<GuestBookJpaEntity, Long> {

    //방명록 목록 조회
    @Query("SELECT g FROM GuestBookJpaEntity g " +
            "JOIN FETCH g.writerId " +
            "WHERE g.ownerId.id = :ownerId " +
            "ORDER BY g.createdAt DESC")
    List<GuestBookJpaEntity> findAllByOwnerIdWithWriter(@Param("ownerId") Long ownerId);
}
