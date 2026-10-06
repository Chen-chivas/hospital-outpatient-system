package com.example.ocs.module.auth.api;

import java.util.Set;

public record MeResponse(long userId, String username, Set<String> roles) {}

