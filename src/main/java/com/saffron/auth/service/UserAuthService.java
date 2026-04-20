package com.saffron.auth.service;

import com.saffron.auth.config.AuthSecretProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService implements UserDetailsService {

    private final AuthSecretProperties authSecretProperties;
    private final PasswordEncoder passwordEncoder;

    // TODO: DB Query로 전환 시 이 메서드만 교체
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authSecretProperties.getUsers().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .map(u -> User.builder()
                .username(u.getUsername())
                .password(passwordEncoder.encode(u.getPassword()))
                .roles(u.getRoles().toArray(new String[0]))
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
