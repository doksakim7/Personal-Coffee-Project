package kr.spartaclub.coffeeproject.domain.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    // 주문 완료 이벤트를 수신하여 로그로 확인한다.
    @KafkaListener(topics = "order-completed", groupId = "coffee-order-group")
    public void consumeOrderCompleted(OrderCompletedEvent event) {
        log.info("주문 완료 이벤트 수신 - orderId={}, userId={}, totalPrice={}, status={}, occurredAt={}",
                event.getOrderId(),
                event.getUserId(),
                event.getTotalPrice(),
                event.getStatus(),
                event.getOccurredAt());
    }

}
