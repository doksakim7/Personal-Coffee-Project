package kr.spartaclub.coffeeproject.domain.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointHistoryResponse {

    private Long historyId;
    private String type;
    private Long amount;
    private Long balance;
    private LocalDateTime createdAt;

}
