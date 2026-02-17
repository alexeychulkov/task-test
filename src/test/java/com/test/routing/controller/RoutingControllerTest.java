package com.test.routing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.test.routing.dto.RouteResponse;
import com.test.routing.exception.NoRouteFoundException;
import com.test.routing.service.RoutingService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RoutingControllerTest {

  private RoutingController routingController;
  private RoutingService routingService;

  @BeforeEach
  void setUp() {
    routingService = mock(RoutingService.class);
    routingController = new RoutingController(routingService);
  }

  @Test
  void shouldReturnRouteWhenPathExists() {
    List<String> expectedRoute = List.of("CZE", "AUT", "ITA");
    when(routingService.calculateRoute("CZE", "ITA")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("CZE", "ITA");

    StepVerifier.create(result)
        .assertNext(response -> assertEquals(expectedRoute, response.route()))
        .verifyComplete();

    verify(routingService).calculateRoute("CZE", "ITA");
  }

  @Test
  void shouldHandleLowercaseInput() {
    List<String> expectedRoute = List.of("CZE", "AUT", "ITA");
    when(routingService.calculateRoute("CZE", "ITA")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("cze", "ita");

    StepVerifier.create(result)
        .assertNext(response -> assertEquals(expectedRoute, response.route()))
        .verifyComplete();

    verify(routingService).calculateRoute("CZE", "ITA");
  }

  @Test
  void shouldHandleMixedCaseInput() {
    List<String> expectedRoute = List.of("CZE", "AUT", "ITA");
    when(routingService.calculateRoute("CZE", "ITA")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("CzE", "ItA");

    StepVerifier.create(result)
        .assertNext(response -> assertEquals(expectedRoute, response.route()))
        .verifyComplete();

    verify(routingService).calculateRoute("CZE", "ITA");
  }

  @Test
  void shouldReturnBadRequestWhenNoRouteExists() {
    when(routingService.calculateRoute("USA", "JPN"))
        .thenReturn(Mono.just(Collections.emptyList()));

    Mono<RouteResponse> result = routingController.getRoute("USA", "JPN");

    StepVerifier.create(result)
        .expectErrorMatches(
            throwable ->
                throwable instanceof NoRouteFoundException
                    && ((NoRouteFoundException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST
                    && ((NoRouteFoundException) throwable)
                        .getMessage()
                        .contains("No land route found"))
        .verify();

    verify(routingService).calculateRoute("USA", "JPN");
  }

  @Test
  void shouldReturnBadRequestWhenCountryDoesNotExist() {
    when(routingService.calculateRoute("XXX", "YYY"))
        .thenReturn(Mono.just(Collections.emptyList()));

    Mono<RouteResponse> result = routingController.getRoute("XXX", "YYY");

    StepVerifier.create(result)
        .expectErrorMatches(
            throwable ->
                throwable instanceof ResponseStatusException
                    && ((ResponseStatusException) throwable).getStatusCode()
                        == HttpStatus.BAD_REQUEST)
        .verify();

    verify(routingService).calculateRoute("XXX", "YYY");
  }

  @Test
  void shouldReturnSingleCountryRouteForSameOriginDestination() {
    List<String> expectedRoute = List.of("CZE");
    when(routingService.calculateRoute("CZE", "CZE")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("CZE", "CZE");

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(1, response.route().size());
              assertEquals("CZE", response.route().get(0));
            })
        .verifyComplete();

    verify(routingService).calculateRoute("CZE", "CZE");
  }

  @Test
  void shouldReturnLongRoute() {
    List<String> expectedRoute = List.of("PRT", "ESP", "FRA", "DEU", "POL", "RUS");
    when(routingService.calculateRoute("PRT", "RUS")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("PRT", "RUS");

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(6, response.route().size());
              assertEquals(expectedRoute, response.route());
            })
        .verifyComplete();

    verify(routingService).calculateRoute("PRT", "RUS");
  }

  @Test
  void shouldHandleServiceError() {
    when(routingService.calculateRoute("CZE", "ITA"))
        .thenReturn(Mono.error(new RuntimeException("Service error")));

    Mono<RouteResponse> result = routingController.getRoute("CZE", "ITA");

    StepVerifier.create(result).expectError(RuntimeException.class).verify();

    verify(routingService).calculateRoute("CZE", "ITA");
  }

  @Test
  void shouldReturnDirectBorderCrossing() {
    List<String> expectedRoute = List.of("CZE", "AUT");
    when(routingService.calculateRoute("CZE", "AUT")).thenReturn(Mono.just(expectedRoute));

    Mono<RouteResponse> result = routingController.getRoute("CZE", "AUT");

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(2, response.route().size());
              assertEquals("CZE", response.route().get(0));
              assertEquals("AUT", response.route().get(1));
            })
        .verifyComplete();

    verify(routingService).calculateRoute("CZE", "AUT");
  }
}
