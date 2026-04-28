package com.saffron.auth.service;

import com.saffron.auth.entity.UserInfo;
import com.saffron.auth.repository.UserInfoRepository;
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

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    // user_info 테이블에서 사용자 정보를 조회.
    // DB에는 평문 비밀번호가 저장되어 있다고 가정하며, 매 로그인마다 BCrypt 인코딩 후
    // Spring Security 의 PasswordEncoder 비교 흐름에 태운다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userInfoRepository.findById(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
            .username(user.getUserId())
            .password(passwordEncoder.encode(user.getPassword()))
            .roles("USER")
            .build();
    }
}
