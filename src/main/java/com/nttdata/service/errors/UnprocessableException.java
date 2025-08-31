package com.nttdata.service.errors;

public class UnprocessableException extends RuntimeException {
  public UnprocessableException(String message) { super(message); }
}