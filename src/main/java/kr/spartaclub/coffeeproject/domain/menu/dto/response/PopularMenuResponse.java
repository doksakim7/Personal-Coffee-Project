package kr.spartaclub.coffeeproject.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularMenuResponse {

    private Long menuId;
    private String name;
    private String type;
    private Long orderCount;

}
