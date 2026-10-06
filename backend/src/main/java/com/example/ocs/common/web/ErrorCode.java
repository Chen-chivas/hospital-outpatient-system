package com.example.ocs.common.web;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  OK(0, "OK", HttpStatus.OK),
  BAD_REQUEST(40000, "Bad request", HttpStatus.BAD_REQUEST),
  NOT_FOUND(40400, "Not found", HttpStatus.NOT_FOUND),
  CONFLICT(40900, "Conflict", HttpStatus.CONFLICT),
  INTERNAL_ERROR(50000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final int code;
  private final String defaultMessage;
  private final HttpStatus httpStatus;

  ErrorCode(int code, String defaultMessage, HttpStatus httpStatus) {
    this.code = code;
    this.defaultMessage = defaultMessage;
    this.httpStatus = httpStatus;
  }

  public int code() {
    return code;
  }

  public String defaultMessage() {
    return defaultMessage;
  }

  public HttpStatus httpStatus() {
    return httpStatus;
  }
}

