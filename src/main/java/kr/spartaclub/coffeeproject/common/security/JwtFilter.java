package kr.spartaclub.coffeeproject.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // 에러 응답 JSON 작성
    private void writeJsonErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(message))
        );
        response.getWriter().flush();
    }

    // JWT Claims → 사용자 정보 변환 (AuthUser 생성)
    private AuthUser createAuthUser(Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        return new AuthUser(userId, email);
    }

    // SecurityContext에 인증 정보 등록 (인가 처리에 사용됨)
    private void setAuthentication(AuthUser authUser) {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        // 토큰 없으면 → 그냥 통과
        if (!hasText(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 토큰 형식이 아니면 → 잘못된 토큰으로 간주 (401)
        if (!authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonErrorResponse(response, ErrorCode.INVALID_TOKEN.getMessage());
            return;
        }

        try {
            // Bearer 제거 → 순수 토큰 추출
            String token = jwtUtil.extractToken(authorizationHeader);

            // JWT 검증 + Claims 추출
            Claims claims = jwtUtil.parseAndValidateToken(token);

            // 인증 객체 생성 및 정보 저장
            AuthUser authUser = createAuthUser(claims);
            setAuthentication(authUser);

            log.debug("JWT 인증 성공 - userId: {}, uri: {}", authUser.getId(), request.getRequestURI());

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            // JWT 관련 예외 → 상태코드 + 메시지 반환
            response.setStatus(e.getErrorCode().getStatus().value());
            writeJsonErrorResponse(response, e.getMessage());
        }
    }

}
