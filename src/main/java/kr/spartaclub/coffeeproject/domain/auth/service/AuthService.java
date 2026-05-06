package kr.spartaclub.coffeeproject.domain.auth.service;

import jakarta.validation.Valid;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.common.security.JwtUtil;
import kr.spartaclub.coffeeproject.domain.auth.dto.request.LoginRequest;
import kr.spartaclub.coffeeproject.domain.auth.dto.request.SignupRequest;
import kr.spartaclub.coffeeproject.domain.auth.dto.response.LoginResponse;
import kr.spartaclub.coffeeproject.domain.auth.dto.response.SignupResponse;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import kr.spartaclub.coffeeproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public SignupResponse signup(@Valid SignupRequest request) {

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 유저 생성
        User user = new User(
                request.getEmail(),
                encodedPassword
        );

        // 4. 저장
        User savedUser = userRepository.save(user);

        // 5. 응답
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    // 로그인
    public LoginResponse login(@Valid LoginRequest request) {

        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_INVALID_LOGIN));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_INVALID_LOGIN);
        }

        // 3. JWT 생성
        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail());

        // 4. 응답
        return new LoginResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail()
        );
    }

}
