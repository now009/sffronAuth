package com.saffron.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth")
public class AuthSecretProperties {

    private List<UserConfig> users;
    private List<ClientConfig> clients;

    @Getter
    @Setter
    public static class UserConfig {
        private String username;
        private String password;
        private List<String> roles;
    }

    @Getter
    @Setter
    public static class ClientConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private List<String> scopes;
    }
}
