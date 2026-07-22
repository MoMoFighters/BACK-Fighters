package com.wanted.momocity.payment.application.supporter;

import com.wanted.momocity.payment.application.port.SetUserMembershipPort;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentStatusUpdater {
    /*comment
    *  결제를 실패해서 롤백할 때 db상에 실패했다는 상태를 저장해야하는데
    *  그러면 실패 처리 롤백이랑은 별도의 트랜잭션에서 db의 상태 없데이터가 이루어져야 해서 만든 클래스 !*/

    private final PaymentRepository paymentRepository;
    private final SetUserMembershipPort setUserMembershipPort;

    /*comment
    *  propagation : 이미 트랜잭션이 있을 때 새로운 메서드 호출을 어떻게 처리할까
    *  - REQUIRED - 기존 트랜잭션에 합류 : 같이 롤백됨
    *  - REQUIRES_NEW - 독립된 트랜잭선 : 기존 게 롤백 되더라도 얘는 커밋
    *  - SUPPORTS : 있으면 기존 거랑 같이 가고 없으면 말고  */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailed(Payment payment) {
        paymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCancelFailed(Payment payment) {
        paymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRefund(Payment refund) { paymentRepository.save(refund);}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMembershipIndependently(Long userId, Plan plan, LocalDateTime membershipStart) {
        setUserMembershipPort.updateMembership(userId, plan, membershipStart);
    }
}
