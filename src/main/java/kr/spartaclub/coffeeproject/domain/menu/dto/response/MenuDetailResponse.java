package kr.spartaclub.coffeeproject.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuDetailResponse {

    private Long menuId;
    private String name;
    private Long price;
    private String status;
    private String type;

}
