package kr.spartaclub.coffeeproject.domain.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_COMPLETED_TOPIC = "order-completed";

    private final KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

    // 주문 완료 이벤트를 Kafka로 발행한다.
    public void sendOrderCompleted(OrderCompletedEvent event) {
        kafkaTemplate.send(
                ORDER_COMPLETED_TOPIC,
                String.valueOf(event.getOrderId()),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("주문 완료 이벤트 발행 실패 - orderId={}", event.getOrderId(), ex);
                return;
            }

            log.info("주문 완료 이벤트 발행 성공 - topic={}, orderId={}",
                    ORDER_COMPLETED_TOPIC,
                    event.getOrderId());
        });
    }

}
