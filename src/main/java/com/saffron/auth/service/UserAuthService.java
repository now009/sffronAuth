package com.saffron.auth.service;

import com.saffron.auth.entity.UserInfo;
import com.saffron.auth.repository.UserInfoRepository;
import com.saffron.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // user_info 에서 사용자, user_role 에서 권한을 함께 로드한다.
    // DB 의 roleCode 가 'ROLE_' 프리픽스 유무와 관계없이 동작하도록 정규화한다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userInfoRepository.findById(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>(
            userRoleRepository.findByUserId(username).stream()
                .map(r -> r.getRoleCode().startsWith("ROLE_") ? r.getRoleCode() : "ROLE_" + r.getRoleCode())
                .map(SimpleGrantedAuthority::new)
                .toList()
        );
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(
            user.getUserId(),
            passwordEncoder.encode(user.getPassword()),
            authorities
        );
    }
}
