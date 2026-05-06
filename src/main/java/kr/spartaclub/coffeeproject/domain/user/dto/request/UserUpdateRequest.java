package kr.spartaclub.coffeeproject.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserUpdateRequest {

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,12}$",
            message = "비밀번호는 8~12자이며, 영문/숫자/특수문자를 각각 최소 1개 포함해야 합니다."
    )
    private String password;

}
