package com.test.routing.service;

import java.util.*;

import reactor.core.publisher.Mono;

public class RoutingService {

  private final CountryService countryService;

  public RoutingService(CountryService countryService) {
    this.countryService = countryService;
  }

  /**
   * Calculates the shortest land route from origin to destination using BFS algorithm. BFS
   * guarantees finding the shortest path in an unweighted graph.
   *
   * @param origin the starting country code (cca3)
   * @param destination the destination country code (cca3)
   * @return Mono containing the list of country codes representing the route, or empty if no route
   *     exists
   */
  public Mono<List<String>> calculateRoute(String origin, String destination) {
    return Mono.fromCallable(
        () -> {
          Map<String, List<String>> borderGraph = countryService.getBorderGraph();

          // Validate that both countries exist
          if (!borderGraph.containsKey(origin) || !borderGraph.containsKey(destination)) {
            return Collections.emptyList();
          }

          // If origin equals destination
          if (origin.equals(destination)) {
            return List.of(origin);
          }

          // BFS to find the shortest path
          Queue<String> queue = new LinkedList<>();
          Map<String, String> parentMap = new HashMap<>();
          Set<String> visited = new HashSet<>();

          queue.offer(origin);
          visited.add(origin);
          parentMap.put(origin, null);

          while (!queue.isEmpty()) {
            String current = queue.poll();

            // Check if we reached the destination
            if (current.equals(destination)) {
              return reconstructPath(parentMap, destination);
            }

            // Explore neighbors (bordering countries)
            List<String> neighbors = borderGraph.getOrDefault(current, Collections.emptyList());
            for (String neighbor : neighbors) {
              if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                parentMap.put(neighbor, current);
                queue.offer(neighbor);
              }
            }
          }

          // No path found
          return Collections.emptyList();
        });
  }

  /** Reconstructs the path from origin to destination using the parent map from BFS. */
  private List<String> reconstructPath(Map<String, String> parentMap, String destination) {
    List<String> path = new ArrayList<>();
    String current = destination;

    while (current != null) {
      path.add(current);
      current = parentMap.get(current);
    }

    Collections.reverse(path);
    return path;
  }
}
