package com.saffron.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "logging.http")
public class HttpLoggingProperties {

    private boolean enabled = true;

    private int maxBodySize = 4096;

    private boolean logStatusCheck = false;
}
