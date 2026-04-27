package com.saffron.auth.controller;

import com.saffron.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Value("${redirect.after-login}")
    private String afterLoginRedirectUrl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("userId") String username,
                                   @RequestParam String password,
                                   HttpServletRequest request) {

        log.info("[AUTH] login attempt - userId: {}, origin: {}", username, request.getHeader("Origin"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            log.info("[AUTH] login success - username: {}", username);

            String accessToken = jwtTokenService.generate(authentication);

            // 토큰을 Backend /main 으로 redirect (Backend가 검증 후 Frontend로 재redirect)
            String redirectUrl = afterLoginRedirectUrl + "?access_token=" + accessToken;
            log.info("[AUTH] redirecting to backend: {}", afterLoginRedirectUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();

        } catch (AuthenticationException e) {
            log.warn("[AUTH] login failed - userId: {}", username);
            return ResponseEntity.status(401).body(Map.of("error", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }
}
