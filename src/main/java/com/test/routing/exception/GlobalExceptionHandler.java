package com.test.routing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import com.test.routing.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoRouteFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleNoRouteFoundException(
      NoRouteFoundException ex, ServerWebExchange exchange) {
    String path = exchange.getRequest().getPath().value();
    return new ErrorResponse(ex.getMessage(), path);
  }
}
