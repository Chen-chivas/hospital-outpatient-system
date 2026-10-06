package com.example.ocs.security;

import java.util.List;

public record OcsPrincipal(long userId, String username, List<String> roles) {}

