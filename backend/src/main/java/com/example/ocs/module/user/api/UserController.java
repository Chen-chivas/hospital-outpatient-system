package com.example.ocs.module.user.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.user.application.UserService;
import com.example.ocs.security.OcsPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  private final UserService userService;
  private final AuditService auditService;

  public UserController(UserService userService, AuditService auditService) {
    this.userService = userService;
    this.auditService = auditService;
  }

  @PostMapping("/api/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var user = userService.create(request);
    auditService.record(principal.userId(), "CREATE", "user", "User", user.getId(), "{\"username\":\"" + user.getUsername() + "\"}");
    return ApiResponse.success(UserResponse.from(user));
  }

  @GetMapping("/api/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<UserResponse>> listUsers() {
    return ApiResponse.success(userService.list().stream().map(UserResponse::from).toList());
  }

  @GetMapping("/api/users/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<UserResponse> getUser(@PathVariable("id") long id) {
    return ApiResponse.success(UserResponse.from(userService.getById(id)));
  }

  @PatchMapping("/api/users/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<UserResponse> updateStatus(
      @PathVariable("id") long id,
      @Valid @RequestBody UpdateUserStatusRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var user = userService.updateStatus(id, request.status());
    auditService.record(principal.userId(), "UPDATE_STATUS", "user", "User", user.getId(), "{\"status\":\"" + user.getStatus().name() + "\"}");
    return ApiResponse.success(UserResponse.from(user));
  }

  @PostMapping("/api/users/{id}/reset-password")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<UserResponse> resetPassword(
      @PathVariable("id") long id,
      @Valid @RequestBody ResetPasswordRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var user = userService.resetPassword(id, request.newPassword());
    auditService.record(principal.userId(), "RESET_PASSWORD", "user", "User", user.getId(), null);
    return ApiResponse.success(UserResponse.from(user));
  }
}
