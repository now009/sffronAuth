package com.saffron.auth.service;

import com.saffron.auth.entity.UserInfo;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        // HS256 은 최소 256bit(32 byte) 키 필요. application.yml 에서 관리.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(UserInfo user, Authentication authentication) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        JwtBuilder builder = Jwts.builder()
            .subject(user.getUserId())
            .claim("userId", user.getUserId())
            .claim("deptId", user.getDeptId())
            .claim("userName", user.getUserName())
            .claim("email", user.getEmail())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key, Jwts.SIG.HS256);

        if (isAdmin) {
            builder.claim("manager", "y");
        }

        return builder.compact();
    }
}
