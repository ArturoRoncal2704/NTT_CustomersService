package com.nttdata.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<String>> handleNotFound(IllegalArgumentException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public Mono<ResponseEntity<String>> handleBadRequest(IllegalStateException ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<String>> handleInternalError(Exception ex) {
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error"));
  }
}
