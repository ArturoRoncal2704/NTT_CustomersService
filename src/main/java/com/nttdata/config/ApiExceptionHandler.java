package com.nttdata.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nttdata.service.errors.ConflictException;
import com.nttdata.service.errors.NotFoundException;
import com.nttdata.service.errors.UnprocessableException;
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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  private static Map<String, Object> problem(int status, String error, String msg, String path) {
    return Map.of("status", status, "error", error, "message", msg, "path", path);
  }

  // 400 - Bean Validation
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleBind(WebExchangeBindException ex, ServerWebExchange exg) {
    var fieldErrors = ex.getFieldErrors().stream().collect(
            Collectors.groupingBy(FieldError::getField,
                    Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())));
    var body = new HashMap<>(problem(400,"Bad Request","Validation failed", exg.getRequest().getPath().value()));
    body.put("errors", fieldErrors);
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
  }

  // 400 - parámetros mal formados (enum inválido, falta query param, etc.)
  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleInput(ServerWebInputException ex, ServerWebExchange exg) {
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(problem(400,"Bad Request", ex.getReason(), exg.getRequest().getPath().value())));
  }

  // 404 - dominio
  @ExceptionHandler(NotFoundException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleNotFound(NotFoundException ex, ServerWebExchange exg) {
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(problem(404,"Not Found", ex.getMessage(), exg.getRequest().getPath().value())));
  }

  // 409 - dominio + datos
  @ExceptionHandler({ ConflictException.class, DuplicateKeyException.class,
          org.springframework.dao.IncorrectResultSizeDataAccessException.class })
  public Mono<ResponseEntity<Map<String, Object>>> handleConflict(RuntimeException ex, ServerWebExchange exg) {
    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
            .body(problem(409,"Conflict", ex.getMessage(), exg.getRequest().getPath().value())));
  }

  // 422 - reglas de negocio
  @ExceptionHandler(UnprocessableException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleUnprocessable(UnprocessableException ex, ServerWebExchange exg) {
    return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(problem(422,"Unprocessable Entity", ex.getMessage(), exg.getRequest().getPath().value())));
  }

  // 500 - fallback (última red)
  @ExceptionHandler({ Exception.class, Throwable.class })
  public Mono<ResponseEntity<Map<String, Object>>> handleAny(Throwable ex, ServerWebExchange exg) {
    log.error("Unexpected error", ex);
    String msg = ex.getClass().getSimpleName() + (ex.getMessage() != null ? (": " + ex.getMessage()) : "");
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problem(500,"Internal Server Error", msg, exg.getRequest().getPath().value())));
  }
}
