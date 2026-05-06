package kr.spartaclub.coffeeproject.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private List<OrderItemDetail> items;
    private Long totalPrice;
    private String status;
    private LocalDateTime createdAt;

    @Getter
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Long menuId;
        private String name;
        private Long price;
        private Integer quantity;
    }

}
