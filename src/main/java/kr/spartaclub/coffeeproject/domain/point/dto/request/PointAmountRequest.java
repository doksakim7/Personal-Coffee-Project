package kr.spartaclub.coffeeproject.domain.point.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PointAmountRequest {

    @NotNull(message = "금액은 필수입니다.")
    private Long amount;

}
