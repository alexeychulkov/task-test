package com.test.routing.service;

import java.net.URI;
import java.util.*;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.routing.model.Country;

public class CountryService {

  private static final Logger log = LoggerFactory.getLogger(CountryService.class);

  private Map<String, List<String>> borderGraph;

  private final ObjectMapper objectMapper;
  private final WebClient webClient;
  private final String countriesJsonUrl;

  public CountryService(
      ObjectMapper objectMapper,
      WebClient webClient,
      @Value("${countries.json.url}") String countriesJsonUrl) {
    this.objectMapper = objectMapper;
    this.webClient = webClient;
    this.countriesJsonUrl = countriesJsonUrl;
  }

  public Map<String, List<String>> getBorderGraph() {
    return borderGraph;
  }

  /**
   * Loads country data from remote JSON URL and builds the border graph. This is executed once at
   * application startup.
   */
  @PostConstruct
  public void loadCountryData() {
    try {
      log.info("Loading country data from: {}", countriesJsonUrl);

      String jsonData =
          webClient
              .get()
              .uri(URI.create(countriesJsonUrl))
              .retrieve()
              .bodyToMono(String.class)
              .block();

      List<Country> countries = objectMapper.readValue(jsonData, new TypeReference<>() {});

      buildBorderGraph(countries);
      log.info("Loaded {} countries successfully from remote URL", countries.size());

    } catch (Exception e) {
      log.error("Failed to load country data from URL: {}", countriesJsonUrl, e);
      throw new RuntimeException("Failed to load country data", e);
    }
  }

  /**
   * Builds a graph representation of country borders for efficient pathfinding. Each country code
   * maps to a list of its neighboring country codes.
   */
  private void buildBorderGraph(List<Country> countries) {
    borderGraph = new HashMap<>();

    for (Country country : countries) {
      if (country.cca3() != null) {
        List<String> borders =
            country.borders() != null ? country.borders() : Collections.emptyList();
        borderGraph.put(country.cca3(), borders);
      }
    }
  }
}
