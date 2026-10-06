package com.example.ocs.common.web;

import com.example.ocs.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    ApiResponse<Void> body = ApiResponse.error(errorCode, exception.getMessage());
    return ResponseEntity.status(errorCode.httpStatus()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST, message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
    String message = exception.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .collect(Collectors.joining("; "));
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST, message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception
  ) {
    return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST, "Invalid JSON request body"));
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ApiResponse<Void>> handleThrowable(Throwable exception) {
    LOGGER.error("Unhandled exception", exception);
    return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.httpStatus())
        .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, null));
  }

  private String formatFieldError(FieldError fieldError) {
    if (fieldError.getDefaultMessage() == null || fieldError.getDefaultMessage().isBlank()) {
      return fieldError.getField() + ": invalid";
    }
    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
  }
}
