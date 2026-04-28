package com.saffron.auth.controller;

import com.saffron.auth.entity.UserInfo;
import com.saffron.auth.repository.UserInfoRepository;
import com.saffron.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${redirect.after-login}")
    private String afterLoginRedirectUrl;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserInfoRepository userInfoRepository;

    @PostMapping("/login")
    public void login(@RequestParam("userId") String username,
                      @RequestParam String password,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {

        // TODO: 운영 전 반드시 비밀번호 로그 제거
        log.info("[AUTH] login attempt - userId: {}, password: {}, origin: {}",
                username, password, request.getHeader("Origin"));

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        log.info("[AUTH] login success - username: {}", username);

        UserInfo user = userInfoRepository.findById(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));

        String token = jwtTokenService.generate(user);

        response.sendRedirect(afterLoginRedirectUrl + "?access_token=" + token);
    }
}
