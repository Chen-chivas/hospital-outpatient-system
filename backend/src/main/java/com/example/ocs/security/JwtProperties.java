package com.example.ocs.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocs.security.jwt")
public record JwtProperties(String issuer, String secret, long expiresMinutes) {}

