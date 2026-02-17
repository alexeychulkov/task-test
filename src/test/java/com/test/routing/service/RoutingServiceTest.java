package com.test.routing.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RoutingServiceTest {

  private RoutingService routingService;
  private CountryService countryService;

  @BeforeEach
  void setUp() {
    countryService = mock(CountryService.class);
    routingService = new RoutingService(countryService);
  }

  @Test
  void shouldFindDirectRoute() {
    // Setup mock data: CZE -> AUT
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT", "DEU", "POL", "SVK"));
    borderGraph.put("AUT", List.of("CZE", "DEU", "ITA", "CHE"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "AUT");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(2, route.size());
              assertEquals("CZE", route.get(0));
              assertEquals("AUT", route.get(1));
            })
        .verifyComplete();
  }

  @Test
  void shouldFindMultiHopRoute() {
    // Setup mock data: CZE -> AUT -> ITA
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT", "DEU", "POL", "SVK"));
    borderGraph.put("AUT", List.of("CZE", "DEU", "ITA", "CHE"));
    borderGraph.put("ITA", List.of("AUT", "FRA", "SVN", "CHE"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "ITA");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(3, route.size());
              assertEquals("CZE", route.get(0));
              assertEquals("AUT", route.get(1));
              assertEquals("ITA", route.get(2));
            })
        .verifyComplete();
  }

  @Test
  void shouldReturnEmptyListWhenNoRouteExists() {
    // Setup mock data: CZE and JPN (no connection)
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT", "DEU", "POL", "SVK"));
    borderGraph.put("JPN", Collections.emptyList());

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "JPN");

    StepVerifier.create(result).assertNext(route -> assertTrue(route.isEmpty())).verifyComplete();
  }

  @Test
  void shouldReturnEmptyListWhenOriginDoesNotExist() {
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("XXX", "CZE");

    StepVerifier.create(result).assertNext(route -> assertTrue(route.isEmpty())).verifyComplete();
  }

  @Test
  void shouldReturnEmptyListWhenDestinationDoesNotExist() {
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "XXX");

    StepVerifier.create(result).assertNext(route -> assertTrue(route.isEmpty())).verifyComplete();
  }

  @Test
  void shouldHandleSameOriginAndDestination() {
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "CZE");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(1, route.size());
              assertEquals("CZE", route.get(0));
            })
        .verifyComplete();
  }

  @Test
  void shouldFindShortestPath() {
    // Setup: Multiple paths exist, should find shortest
    // CZE -> DEU -> FRA (2 hops)
    // CZE -> AUT -> ITA -> FRA (3 hops)
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT", "DEU"));
    borderGraph.put("DEU", List.of("CZE", "FRA"));
    borderGraph.put("AUT", List.of("CZE", "ITA"));
    borderGraph.put("ITA", List.of("AUT", "FRA"));
    borderGraph.put("FRA", List.of("DEU", "ITA"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "FRA");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(3, route.size());
              assertEquals("CZE", route.get(0));
              assertEquals("DEU", route.get(1));
              assertEquals("FRA", route.get(2));
            })
        .verifyComplete();
  }

  @Test
  void shouldHandleLongRoute() {
    // Setup: A -> B -> C -> D -> E
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("A", List.of("B"));
    borderGraph.put("B", List.of("A", "C"));
    borderGraph.put("C", List.of("B", "D"));
    borderGraph.put("D", List.of("C", "E"));
    borderGraph.put("E", List.of("D"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("A", "E");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(5, route.size());
              assertEquals(List.of("A", "B", "C", "D", "E"), route);
            })
        .verifyComplete();
  }

  @Test
  void shouldHandleComplexGraph() {
    // Complex graph with multiple connections
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", List.of("AUT", "DEU", "POL", "SVK"));
    borderGraph.put("AUT", List.of("CZE", "DEU", "ITA", "SVN", "CHE", "HUN", "SVK"));
    borderGraph.put("DEU", List.of("CZE", "AUT", "POL", "CHE", "FRA"));
    borderGraph.put("ITA", List.of("AUT", "FRA", "SVN", "CHE"));
    borderGraph.put("FRA", List.of("DEU", "ITA", "CHE"));
    borderGraph.put("CHE", List.of("AUT", "DEU", "ITA", "FRA"));
    borderGraph.put("POL", List.of("CZE", "DEU"));
    borderGraph.put("SVK", List.of("CZE", "AUT"));
    borderGraph.put("SVN", List.of("AUT", "ITA"));
    borderGraph.put("HUN", List.of("AUT"));

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "ITA");

    StepVerifier.create(result)
        .assertNext(
            route -> {
              assertEquals(3, route.size());
              assertEquals("CZE", route.get(0));
              assertEquals("AUT", route.get(1));
              assertEquals("ITA", route.get(2));
            })
        .verifyComplete();
  }

  @Test
  void shouldHandleEmptyBordersList() {
    Map<String, List<String>> borderGraph = new HashMap<>();
    borderGraph.put("CZE", Collections.emptyList());
    borderGraph.put("AUT", Collections.emptyList());

    when(countryService.getBorderGraph()).thenReturn(borderGraph);

    Mono<List<String>> result = routingService.calculateRoute("CZE", "AUT");

    StepVerifier.create(result).assertNext(route -> assertTrue(route.isEmpty())).verifyComplete();
  }
}
