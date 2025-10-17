package com.cryptoAlert;


import com.cryptoAlert.dto.request.LoginRequest;
import com.cryptoAlert.dto.response.TokenResponse;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.UserRepository;
import com.cryptoAlert.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 여기서 refreshToken 저장 (DB/Redis) 가능

        return new TokenResponse(accessToken, refreshToken);
    }
}
