package com.example.ocs.module.auth.api;

import java.util.Set;

public record LoginResponse(String token, long userId, String username, String displayName, Set<String> roles) {}

