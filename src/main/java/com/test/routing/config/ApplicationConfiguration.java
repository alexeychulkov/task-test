package com.test.routing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.routing.service.CountryService;
import com.test.routing.service.RoutingService;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public WebClient webClient() {
    // Increase buffer size to 2MB to handle large JSON responses
    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    return WebClient.builder().exchangeStrategies(strategies).build();
  }

  @Bean
  public CountryService countryService(
      ObjectMapper objectMapper,
      WebClient webClient,
      @Value("${countries.json.url}") String countriesJsonUrl) {
    return new CountryService(objectMapper, webClient, countriesJsonUrl);
  }

  @Bean
  public RoutingService routingService(CountryService countryService) {
    return new RoutingService(countryService);
  }
}
