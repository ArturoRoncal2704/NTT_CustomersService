package com.nttdata.service.errors;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) { super(message); }
}