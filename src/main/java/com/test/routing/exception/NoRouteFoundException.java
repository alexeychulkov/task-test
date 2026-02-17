package com.test.routing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoRouteFoundException extends ResponseStatusException {

  public NoRouteFoundException(String origin, String destination) {
    super(
        HttpStatus.BAD_REQUEST,
        String.format("No land route found between %s and %s", origin, destination));
  }
}
