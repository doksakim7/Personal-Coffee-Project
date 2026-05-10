package kr.spartaclub.coffeeproject.domain.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletedEventListener {

    private final OrderEventProducer orderEventProducer;

    // 트랜잭션 커밋이 완료된 뒤 주문 완료 이벤트를 Kafka로 발행한다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderCompletedApplicationEvent event) {
        orderEventProducer.sendOrderCompleted(
                new OrderCompletedEvent(
                        event.getOrderId(),
                        event.getUserId(),
                        event.getTotalPrice(),
                        event.getStatus(),
                        LocalDateTime.now()
                )
        );

        log.info("주문 완료 AFTER_COMMIT 이벤트 처리 - orderId={}", event.getOrderId());
    }

}
