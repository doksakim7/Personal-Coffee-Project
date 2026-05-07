package kr.spartaclub.coffeeproject.domain.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PointHistoryListResponse {

    private List<PointHistoryItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @AllArgsConstructor
    public static class PointHistoryItem {
        private Long historyId;
        private String type;
        private Long amount;
        private Long balance;
        private LocalDateTime createdAt;
    }
}