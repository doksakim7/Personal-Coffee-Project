package kr.spartaclub.coffeeproject.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MenuListResponse {

    private List<MenuSummary> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @AllArgsConstructor
    public static class MenuSummary {
        private Long menuId;
        private String name;
        private Long price;
        private String status;
        private String type;
    }

}
