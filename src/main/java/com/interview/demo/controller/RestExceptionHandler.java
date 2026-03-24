package com.interview.demo.controller;

import com.interview.demo.exception.ConflictException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.exception.ValidationException;
import java.util.Comparator;
import java.util.Optional;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
  private static final String ERROR_KEY = "error";

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, String>> handleConflictException(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_KEY, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .min(Comparator.comparingInt(this::validationPriority))
            .map(fieldError -> fieldError.getDefaultMessage())
            .or(() -> Optional.ofNullable(ex.getBindingResult().getFieldError()).map(fieldError -> fieldError.getDefaultMessage()))
            .filter(msg -> !msg.isBlank())
            .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, message));
  }

  private int validationPriority(org.springframework.validation.FieldError fieldError) {
    return switch (fieldError.getCode()) {
      case "NotBlank", "NotNull" -> 0;
      default -> 1;
    };
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    String message =
        ex.getConstraintViolations().stream()
            .min(
                Comparator.comparingInt(
                    violation ->
                        constraintPriority(
                            violation
                                .getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                .getSimpleName())))
            .map(violation -> violation.getMessage())
            .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, message));
  }

  private int constraintPriority(String annotationSimpleName) {
    return switch (annotationSimpleName) {
      case "NotBlank", "NotNull" -> 0;
      default -> 1;
    };
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(ERROR_KEY, "Unexpected server error"));
  }
}
