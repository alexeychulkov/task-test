package com.test.routing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.routing.dto.RouteResponse;
import com.test.routing.exception.NoRouteFoundException;
import com.test.routing.service.RoutingService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/routing")
public class RoutingController {

  private final RoutingService routingService;

  public RoutingController(RoutingService routingService) {
    this.routingService = routingService;
  }

  /**
   * Calculates and returns a land route from origin to destination country.
   *
   * @param origin the starting country code (cca3)
   * @param destination the destination country code (cca3)
   * @return Mono containing RouteResponse with the calculated route
   * @throws NoRouteFoundException with HTTP 400 if no land route exists
   */
  @GetMapping("/{origin}/{destination}")
  public Mono<RouteResponse> getRoute(
      @PathVariable String origin, @PathVariable String destination) {

    String originUpper = origin.toUpperCase();
    String destinationUpper = destination.toUpperCase();

    return routingService
        .calculateRoute(originUpper, destinationUpper)
        .flatMap(
            route -> {
              if (route.isEmpty()) {
                return Mono.error(new NoRouteFoundException(originUpper, destinationUpper));
              }
              return Mono.just(new RouteResponse(route));
            });
  }
}
