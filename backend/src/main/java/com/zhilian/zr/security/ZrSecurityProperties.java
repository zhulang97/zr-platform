package com.zhilian.zr.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zr.security.jwt")
public record ZrSecurityProperties(
    String issuer,
    String secret,
    long accessTtlSeconds,
    long refreshTtlSeconds
) {
}
