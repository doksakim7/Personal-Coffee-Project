package kr.spartaclub.coffeeproject.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderSummaryItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @AllArgsConstructor
    public static class OrderSummaryItem {
        private Long orderId;
        private Long totalPrice;
        private String status;
        private LocalDateTime createdAt;
    }
}