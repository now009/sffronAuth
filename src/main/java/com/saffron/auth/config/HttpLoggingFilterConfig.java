package com.saffron.auth.config;

import com.saffron.auth.filter.HttpLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class HttpLoggingFilterConfig {

    // Spring Security FilterChainProxy(기본 order ≈ HIGHEST_PRECEDENCE+900)보다 먼저 실행되도록
    // HIGHEST_PRECEDENCE 로 등록 → 인증 실패(401/403) 트래픽까지 모두 로깅됨.
    @Bean
    @ConditionalOnProperty(prefix = "logging.http", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilterRegistration(HttpLoggingProperties properties) {
        FilterRegistrationBean<HttpLoggingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new HttpLoggingFilter(properties));
        reg.addUrlPatterns("/*");
        reg.setName("httpLoggingFilter");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
