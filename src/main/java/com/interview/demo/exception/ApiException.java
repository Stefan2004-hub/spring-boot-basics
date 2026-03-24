package com.interview.demo.exception;

public abstract class ApiException extends RuntimeException {
  protected ApiException(String message) {
    super(message);
  }
}
