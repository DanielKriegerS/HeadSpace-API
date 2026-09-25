package com.headspace.identity.infrastructure.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "headspace.security")
public record SecurityProperties(
        List<String> allowedOrigins,
        String frontendBaseUrl,
        String sessionCookieName,
        String csrfCookieName,
        String csrfHeaderName
) {
}
