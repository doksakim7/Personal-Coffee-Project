package kr.spartaclub.coffeeproject.common.config;

import jakarta.servlet.http.HttpServletResponse;
import kr.spartaclub.coffeeproject.common.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)       // REST API는 세션을 사용하지 않으므로 CSRF 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // 기본 인증 방식 비활성화 (JWT 사용)
                .formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화 (JWT 기반 인증)

                // JWT 인증 필터 등록 (UsernamePasswordAuthenticationFilter 이전에 실행)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 세션을 사용하지 않는 Stateless 구조 설정
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()      // 인증 없이 접근 허용 (로그인/회원가입)
                        .requestMatchers("/api/menus/**").permitAll()     // 메뉴 조회는 공개 API
                        .requestMatchers("/actuator/health").permitAll()  // 서버 상태 확인
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )

                // 인증/인가 실패 시 JSON 응답 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .build();
    }

    // 비밀번호 BCrypt 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
