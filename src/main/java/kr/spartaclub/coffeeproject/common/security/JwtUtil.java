package kr.spartaclub.coffeeproject.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000L; // 토큰 만료 시간 - 60분

    @Value("${jwt.secret.key}")
    private String secretKey;
    private SecretKey key;

    // Base64로 인코딩된 시크릿 키를 디코딩하여 HMAC-SHA 키 객체로 변환
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createToken(Long userId, String email) {
        Date date = new Date();

        return Jwts.builder().subject(String.valueOf(userId))
                .claim("email", email)
                .expiration(new Date(date.getTime() + TOKEN_EXPIRATION_TIME))
                .issuedAt(date)
                .signWith(key)
                .compact();
    }

    // Authorization 헤더에서 토큰 추출
    public String extractToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    // JWT 검증 후 Claims 반환 (인증 처리 단일 진입점)
    public Claims parseAndValidateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료");
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰");
            throw new CustomException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException | SecurityException e) {
            log.warn("유효하지 않은 JWT 토큰");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            log.error("JWT 처리 중 서버 오류", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
