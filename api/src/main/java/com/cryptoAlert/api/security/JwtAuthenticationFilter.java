package com.cryptoAlert.api.security;

import com.cryptoAlert.CustomUserDetailsService;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.security.JwtTokenProvider;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        List<String> excludedPaths = List.of(
                "/api/auth/login",
                "/api/users/signup",
                "/api/auth/refresh",
                "/api/alert/send",
                "/api/alert/test-email", // 간단한 HTML 이메일 테스트
                "/api/alerts",           // Alert Settings 관련 모든 엔드포인트
                "/api/sms",              // Solapi SMS 관련 모든 엔드포인트
                "/api/alert-history",    // Alert History 관련 엔드포인트
                "/swagger-ui",
                "/v3/api-docs",
                "/swagger-resources",
                "/h2-console"
        );

        // ❗ 특정 경로는 토큰 검증 건너뜀
        boolean shouldSkip = excludedPaths.stream().anyMatch(path::startsWith);
        if (shouldSkip) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookie(request, "AccessToken");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.extractUserId(token);

            User user = userDetailsService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
