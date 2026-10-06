package com.example.ocs.module.auth.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.auth.api.LoginRequest;
import com.example.ocs.module.auth.api.LoginResponse;
import com.example.ocs.module.user.infra.UserRepository;
import com.example.ocs.security.JwtTokenService;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtTokenService tokenService;

  public AuthService(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      JwtTokenService tokenService
  ) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.tokenService = tokenService;
  }

  public LoginResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    } catch (AuthenticationException e) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid username or password");
    }

    var user = userRepository.findByUsernameIgnoreCase(request.username())
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "invalid username or password"));
    if (!user.isActive()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "user disabled");
    }

    var roleCodes = user.getRoles().stream().map(r -> r.getCode()).toList();
    String token = tokenService.generateToken(user.getId(), user.getUsername(), roleCodes);

    return new LoginResponse(
        token,
        user.getId(),
        user.getUsername(),
        user.getDisplayName(),
        Set.copyOf(roleCodes)
    );
  }
}

