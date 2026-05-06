package kr.spartaclub.coffeeproject.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class AuthUser implements UserDetails {

    private final Long id; // 사용자 식별자
    private final String email; // 사용자 이메일 (username으로 사용)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // 권한 정보 (현재는 권한 미사용)
    }

    @Override
    public String getPassword() {
        return ""; // 비밀번호 미사용 (JWT 인증)
    }

    @Override
    public String getUsername() {
        return email; // Spring Security에서 사용할 username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (항상 활성)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (항상 활성)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 (항상 활성)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (항상 활성)
    }

}
