package com.test.routing.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Country(String cca3, List<String> borders) {

  public Country {
    // Defensive copy to ensure immutability
    borders = borders == null ? Collections.emptyList() : List.copyOf(borders);
  }
}
