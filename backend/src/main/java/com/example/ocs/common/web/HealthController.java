package com.example.ocs.common.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/api/ping")
  public ApiResponse<String> ping() {
    return ApiResponse.success("pong");
  }
}

