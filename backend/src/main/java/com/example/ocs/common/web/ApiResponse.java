package com.example.ocs.common.web;

import java.time.Instant;

public record ApiResponse<T>(int code, String message, T data, Instant timestamp) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(ErrorCode.OK.code(), ErrorCode.OK.defaultMessage(), data, Instant.now());
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
    String finalMessage = (message == null || message.isBlank()) ? errorCode.defaultMessage() : message;
    return new ApiResponse<>(errorCode.code(), finalMessage, null, Instant.now());
  }
}

