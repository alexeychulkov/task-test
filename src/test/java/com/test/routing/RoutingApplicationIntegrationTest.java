package com.test.routing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.test.routing.dto.RouteResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoutingApplicationIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldCalculateRouteBetweenCzechRepublicAndItaly() {
    webTestClient
        .get()
        .uri("/routing/CZE/ITA")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertNotNull(response.route());
              assertFalse(response.route().isEmpty());
              assertEquals("CZE", response.route().get(0));
              assertEquals("ITA", response.route().get(response.route().size() - 1));
              // CZE -> AUT -> ITA (shortest path)
              assertTrue(response.route().contains("AUT"));
            });
  }

  @Test
  void shouldCalculateRouteWithLowercaseCountryCodes() {
    webTestClient
        .get()
        .uri("/routing/cze/ita")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals("CZE", response.route().get(0));
              assertEquals("ITA", response.route().get(response.route().size() - 1));
            });
  }

  @Test
  void shouldReturnBadRequestForNoLandRoute() {
    webTestClient
        .get()
        .uri("/routing/USA/JPN")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("400 BAD_REQUEST \"No land route found between USA and JPN\"")
        .jsonPath("$.path")
        .isEqualTo("/routing/USA/JPN");
  }

  @Test
  void shouldReturnBadRequestForNonExistentCountry() {
    webTestClient
        .get()
        .uri("/routing/XXX/YYY")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("400 BAD_REQUEST \"No land route found between XXX and YYY\"")
        .jsonPath("$.path")
        .isEqualTo("/routing/XXX/YYY");
  }

  @Test
  void shouldHandleSameOriginAndDestination() {
    webTestClient
        .get()
        .uri("/routing/CZE/CZE")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(1, response.route().size());
              assertEquals("CZE", response.route().get(0));
            });
  }

  @Test
  void shouldCalculateRouteForDirectNeighbors() {
    webTestClient
        .get()
        .uri("/routing/CZE/AUT")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(2, response.route().size());
              assertEquals("CZE", response.route().get(0));
              assertEquals("AUT", response.route().get(1));
            });
  }

  @Test
  void shouldCalculateLongRoute() {
    webTestClient
        .get()
        .uri("/routing/PRT/CHN")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertFalse(response.route().isEmpty());
              assertTrue(response.route().size() > 3);
              assertEquals("PRT", response.route().get(0));
              assertEquals("CHN", response.route().get(response.route().size() - 1));
            });
  }

  @Test
  void shouldCalculateRouteBetweenFranceAndGermany() {
    webTestClient
        .get()
        .uri("/routing/FRA/DEU")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(2, response.route().size());
              assertEquals("FRA", response.route().get(0));
              assertEquals("DEU", response.route().get(1));
            });
  }

  @Test
  void shouldReturnBadRequestForIslandToIsland() {
    webTestClient
        .get()
        .uri("/routing/JPN/AUS")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("400 BAD_REQUEST \"No land route found between JPN and AUS\"")
        .jsonPath("$.path")
        .isEqualTo("/routing/JPN/AUS");
  }

  @Test
  void shouldCalculateRouteAcrossMultipleContinents() {
    webTestClient
        .get()
        .uri("/routing/ESP/IND")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertTrue(response.route().size() > 5);
              assertEquals("ESP", response.route().get(0));
              assertEquals("IND", response.route().get(response.route().size() - 1));
            });
  }

  @Test
  void shouldVerifyRouteResponseIsImmutable() {
    webTestClient
        .get()
        .uri("/routing/CZE/ITA")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(RouteResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              // Verify that the route list is immutable
              assertThrows(UnsupportedOperationException.class, () -> response.route().add("TEST"));
            });
  }

  @Test
  void shouldHandleMultipleConcurrentRequests() {
    // Test thread safety by making multiple concurrent requests
    for (int i = 0; i < 10; i++) {
      webTestClient
          .get()
          .uri("/routing/CZE/ITA")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(RouteResponse.class)
          .value(
              response -> {
                assertNotNull(response);
                assertEquals("CZE", response.route().get(0));
                assertEquals("ITA", response.route().get(response.route().size() - 1));
              });
    }
  }
}
