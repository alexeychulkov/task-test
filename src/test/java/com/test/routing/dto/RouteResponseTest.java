package com.test.routing.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RouteResponseTest {

  @Test
  void shouldCreateRouteResponseWithValidData() {
    List<String> route = List.of("CZE", "AUT", "ITA");
    RouteResponse response = new RouteResponse(route);

    assertEquals(3, response.route().size());
    assertEquals("CZE", response.route().get(0));
    assertEquals("AUT", response.route().get(1));
    assertEquals("ITA", response.route().get(2));
  }

  @Test
  void shouldCreateImmutableRouteList() {
    List<String> route = new ArrayList<>();
    route.add("CZE");
    route.add("AUT");

    RouteResponse response = new RouteResponse(route);

    // Modifying original list should not affect the response's route
    route.add("ITA");

    assertEquals(2, response.route().size());
    assertFalse(response.route().contains("ITA"));
  }

  @Test
  void shouldNotAllowModificationOfRouteList() {
    List<String> route = List.of("CZE", "AUT", "ITA");
    RouteResponse response = new RouteResponse(route);

    assertThrows(UnsupportedOperationException.class, () -> response.route().add("DEU"));
  }

  @Test
  void shouldHandleNullRoute() {
    RouteResponse response = new RouteResponse(null);

    assertNotNull(response.route());
    assertTrue(response.route().isEmpty());
  }

  @Test
  void shouldHandleEmptyRoute() {
    RouteResponse response = new RouteResponse(List.of());

    assertNotNull(response.route());
    assertTrue(response.route().isEmpty());
  }

  @Test
  void shouldBeEqualWhenSameData() {
    RouteResponse response1 = new RouteResponse(List.of("CZE", "AUT", "ITA"));
    RouteResponse response2 = new RouteResponse(List.of("CZE", "AUT", "ITA"));

    assertEquals(response1, response2);
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenDifferentData() {
    RouteResponse response1 = new RouteResponse(List.of("CZE", "AUT", "ITA"));
    RouteResponse response2 = new RouteResponse(List.of("CZE", "DEU", "ITA"));

    assertNotEquals(response1, response2);
  }

  @Test
  void shouldCreateSingleCountryRoute() {
    RouteResponse response = new RouteResponse(List.of("CZE"));

    assertEquals(1, response.route().size());
    assertEquals("CZE", response.route().get(0));
  }

  @Test
  void shouldReturnImmutableEmptyListForNullRoute() {
    RouteResponse response = new RouteResponse(null);

    assertThrows(UnsupportedOperationException.class, () -> response.route().add("XXX"));
  }

  @Test
  void shouldHandleLongRoute() {
    List<String> longRoute = List.of("A", "B", "C", "D", "E", "F", "G", "H");
    RouteResponse response = new RouteResponse(longRoute);

    assertEquals(8, response.route().size());
    assertEquals("A", response.route().get(0));
    assertEquals("H", response.route().get(7));
  }
}
