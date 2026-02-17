package com.test.routing.dto;

import java.util.Collections;
import java.util.List;

public record RouteResponse(List<String> route) {

  public RouteResponse {
    // Defensive copy to ensure immutability
    route = route == null ? Collections.emptyList() : List.copyOf(route);
  }
}
