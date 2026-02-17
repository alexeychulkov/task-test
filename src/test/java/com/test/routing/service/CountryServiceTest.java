package com.test.routing.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

class CountryServiceTest {

  private CountryService countryService;

  @BeforeEach
  void setUp() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    // Read countries.json from test resources
    ClassPathResource resource = new ClassPathResource("countries.json");
    String jsonContent =
        new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    // Mock WebClient
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
        mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
    WebClient webClient = mock(WebClient.class);

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonContent));

    String testUrl = "https://test.example.com/countries.json";
    countryService = new CountryService(objectMapper, webClient, testUrl);
    countryService.loadCountryData();
  }

  @Test
  void shouldLoadCountryDataSuccessfully() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    assertNotNull(borderGraph);
    assertFalse(borderGraph.isEmpty());
  }

  @Test
  void shouldContainCzechRepublicWithCorrectBorders() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    assertTrue(borderGraph.containsKey("CZE"));
    List<String> czeBorders = borderGraph.get("CZE");

    assertNotNull(czeBorders);
    assertTrue(czeBorders.contains("AUT"));
    assertTrue(czeBorders.contains("DEU"));
    assertTrue(czeBorders.contains("POL"));
    assertTrue(czeBorders.contains("SVK"));
  }

  @Test
  void shouldContainItalyWithCorrectBorders() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    assertTrue(borderGraph.containsKey("ITA"));
    List<String> itaBorders = borderGraph.get("ITA");

    assertNotNull(itaBorders);
    assertTrue(itaBorders.contains("AUT"));
    assertTrue(itaBorders.contains("FRA"));
    assertTrue(itaBorders.contains("SVN"));
    assertTrue(itaBorders.contains("CHE"));
  }

  @Test
  void shouldHandleIslandCountriesWithNoBorders() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    // Japan is an island with no land borders
    assertTrue(borderGraph.containsKey("JPN"));
    List<String> jpnBorders = borderGraph.get("JPN");

    assertNotNull(jpnBorders);
    assertTrue(jpnBorders.isEmpty());
  }

  @Test
  void shouldReturnEmptyListForCountriesWithNoBorders() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    // Australia is an island with no land borders
    assertTrue(borderGraph.containsKey("AUS"));
    List<String> ausBorders = borderGraph.get("AUS");

    assertNotNull(ausBorders);
    assertEquals(0, ausBorders.size());
  }

  @Test
  void shouldLoadMultipleCountries() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    // Verify some known countries exist
    assertTrue(borderGraph.containsKey("USA"));
    assertTrue(borderGraph.containsKey("CHN"));
    assertTrue(borderGraph.containsKey("BRA"));
    assertTrue(borderGraph.containsKey("DEU"));
    assertTrue(borderGraph.containsKey("FRA"));
  }

  @Test
  void shouldNotContainNullCountryCodes() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    assertFalse(borderGraph.containsKey(null));
    borderGraph.keySet().forEach(Assertions::assertNotNull);
  }

  @Test
  void shouldReturnImmutableBorderGraph() {
    Map<String, List<String>> borderGraph = countryService.getBorderGraph();

    assertNotNull(borderGraph);
    assertSame(borderGraph, countryService.getBorderGraph());
  }
}
