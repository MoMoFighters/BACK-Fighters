package com.wanted.momocity.fortune.infrastructure.adapter;

import com.wanted.momocity.fortune.application.port.FortunePointPort;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class FortunePointAdapter implements FortunePointPort {

    private final EntityManager entityManager;

    @Override
    // 반드시 상위 서비스의 트랜잭션 안에서 실행되도록 제한
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean deductPointIfEnough(Long userId, Long amount) {
        int updatedRows = entityManager.createNativeQuery(
                """
                        UPDATE `user`
                        SET `point` = `point` - :amount
                        WHERE `id` = :userId
                          AND `point` >= :amount
                        """
        )
                .setParameter("userId", userId)
                .setParameter("amount", amount)
                .executeUpdate();
        return updatedRows == 1;
    }
}
