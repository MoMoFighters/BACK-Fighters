package com.wanted.momocity.payment.infrastructure.applier;

import com.wanted.momocity.payment.domain.exception.PaymentNotFoundException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentRefetcher {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Payment refetch(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));
    }

    /*comment
     * paymentVerify()의 재조회 루프 전용
     * paymentVerify()는 클래스 레벨 @Transactional 때문에
     * 메서드 전체가 하나의 영속성 컨텍스트 안에서 돌고 그 안에서 같은 paymentId를
     * 반복 조회하면 JPA 1차 캐시 때문에 DB에 다시 가지 않고 캐시된 엣날 객체를
     * 그대로 돌려줌 그래서 웹훅이 다른 트랜잭션에서 이미 커밋해놨어도 이쪽에선 못 봄
     * REQUIRES_NEW로 매번 새 트랜잭션을 열어서
     * 진짜 최신 DB 상태를 읽어오기 위해 별도 컴포넌트로 분리
     */
}
