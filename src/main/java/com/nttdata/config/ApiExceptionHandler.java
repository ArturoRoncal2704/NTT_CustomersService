package com.nttdata.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  private static Map<String, Object> problem(int status, String error, String message, String path) {
    return Map.of(
            "status", status,
            "error",  error,
            "message", message,
            "path", path
    );
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleValidation(
          WebExchangeBindException ex, ServerWebExchange exchange) {

    Map<String, List<String>> errors = ex.getFieldErrors().stream()
            .collect(Collectors.groupingBy(FieldError::getField,
                    Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())));

    Map<String, Object> body = Map.of(
            "status", 400,
            "error", "Bad Request",
            "message", "Validation failed",
            "path", exchange.getRequest().getPath().value(),
            "errors", errors
    );

    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
  }

  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleInput(
          ServerWebInputException ex, ServerWebExchange exchange) {

    Map<String, Object> body = problem(
            400, "Bad Request",
            ex.getReason() != null ? ex.getReason() : "Malformed input",
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleDuplicate(
          DuplicateKeyException ex, ServerWebExchange exchange) {

    Map<String, Object> body = problem(
            409, "Conflict", "Document already exists",
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleRse(
          ResponseStatusException ex, ServerWebExchange exchange) {
    int code = ex.getStatus().value();
    String reason = ex.getStatus().getReasonPhrase();

    Map<String, Object> body = problem(
            code, reason,
            ex.getReason() != null ? ex.getReason() : reason,
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(ex.getStatus()).body(body));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(
          IllegalArgumentException ex, ServerWebExchange exchange) {

    Map<String, Object> body = problem(
            404, "Not Found",
            ex.getMessage() != null ? ex.getMessage() : "Resource not found",
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
  }

  @ExceptionHandler(IllegalStateException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleIllegalState(
          IllegalStateException ex, ServerWebExchange exchange) {

    Map<String, Object> body = problem(
            400, "Bad Request",
            ex.getMessage() != null ? ex.getMessage() : "Bad request",
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleAny(
          Exception ex, ServerWebExchange exchange) {

    log.error("Unexpected error", ex);
    Map<String, Object> body = problem(
            500, "Internal Server Error",
            ex.getClass().getSimpleName() + (ex.getMessage() != null ? (": " + ex.getMessage()) : ""),
            exchange.getRequest().getPath().value()
    );
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
  }
}
