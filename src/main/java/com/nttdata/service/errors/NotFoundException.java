package com.nttdata.service.errors;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) { super(message); }
}