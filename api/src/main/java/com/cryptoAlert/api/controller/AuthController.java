package com.cryptoAlert.api.controller;

import com.cryptoAlert.AuthService;
import com.cryptoAlert.UserService;
import com.cryptoAlert.dto.request.LoginRequest;
import com.cryptoAlert.dto.request.UserUpdateRequest;
import com.cryptoAlert.dto.response.TokenResponse;
import com.cryptoAlert.dto.response.UserResponse;
import com.cryptoAlert.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokens = authService.login(request);

        // ✅ access_token 쿠키 생성
        Cookie accessCookie = new Cookie("AccessToken", tokens.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // HTTPS 환경에서는 true로!
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60); // 1시간
        accessCookie.setDomain("localhost"); // local 환경 명시 (선택)

        response.addCookie(accessCookie);

        // ✅ (선택) refresh_token도 쿠키로 저장하려면 아래도 추가
//    Cookie refreshCookie = new Cookie("refresh_token", tokens.getRefreshToken());
//    refreshCookie.setHttpOnly(true);
//    refreshCookie.setSecure(false);
//    refreshCookie.setPath("/");
//    refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 일주일
//    refreshCookie.setDomain("localhost");
//    response.addCookie(refreshCookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AccessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // prod는 true
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),  // 여기 수정됨
                user.getEmail(),
                user.getPhoneNumber()));
    }

    @Operation(summary = "사용자 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UserUpdateRequest request, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserResponse updatedUser = userService.update(user.getId(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
