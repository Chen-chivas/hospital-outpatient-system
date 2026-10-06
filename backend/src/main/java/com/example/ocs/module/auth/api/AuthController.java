package com.example.ocs.module.auth.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.auth.application.AuthService;
import com.example.ocs.security.OcsPrincipal;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/api/auth/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request));
  }

  @GetMapping("/api/auth/me")
  public ApiResponse<MeResponse> me(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof OcsPrincipal principal)) {
      return ApiResponse.success(new MeResponse(-1, "anonymous", Set.of()));
    }
    return ApiResponse.success(new MeResponse(principal.userId(), principal.username(), Set.copyOf(principal.roles())));
  }
}

