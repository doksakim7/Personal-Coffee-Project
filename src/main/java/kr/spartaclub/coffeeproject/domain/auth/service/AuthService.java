package kr.spartaclub.coffeeproject.domain.auth.service;

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
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 이메일 중복을 확인한 뒤 비밀번호를 암호화하여 회원을 저장한다.
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),
                encodedPassword
        );

        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    // 이메일/비밀번호를 검증한 뒤 JWT를 발급하여 반환한다.
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_INVALID_LOGIN));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_INVALID_LOGIN);
        }

        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail());

        return new LoginResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail()
        );
    }

}
