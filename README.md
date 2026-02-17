# Country Routing Service

A reactive Spring Boot service that calculates land routes between countries using border information.

## Features

- **Reactive Programming**: Built with Spring WebFlux for non-blocking, reactive API
- **Efficient Algorithm**: Uses BFS (Breadth-First Search) to find the shortest path between countries
- **REST API**: Simple endpoint to calculate routes between any two countries
- **Error Handling**: Returns HTTP 400 with detailed error response when no land route exists
- **Remote Data Source**: Loads country data from GitHub repository at startup

## Technologies

- Spring Boot 3.2.1
- Spring WebFlux (Reactive)
- Maven
- Java 17
- Jackson
- Spotless (Code formatting)

## Data Source

Country data is loaded from: `https://raw.githubusercontent.com/mledoze/countries/master/countries.json`

The application fetches this data at startup using WebClient and builds an in-memory graph of country borders for efficient route calculation.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for loading country data at startup)

## Building the Application

To build the application, run:

```bash
mvn clean package
```

This will:
1. Download all dependencies
2. Compile the source code
3. Run tests
4. Create an executable JAR file in the `target` directory

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using the JAR file

```bash
java -jar target/task-test-1.0-SNAPSHOT.jar
```

The application will start on port **8080** by default.

## API Usage

### Endpoint

```
GET /routing/{origin}/{destination}
```

### Parameters

- `origin`: The 3-letter country code (cca3) of the starting country
- `destination`: The 3-letter country code (cca3) of the destination country

### Success Response (HTTP 200)

```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

### Error Response (HTTP 400)

When no land route exists between countries (e.g., separated by ocean):

```json
{
  "message": "No land route found between USA and JPN",
  "path": "/routing/USA/JPN"
}
```

## Example Requests

### Using curl

```bash
# Route from Czech Republic to Italy
curl http://localhost:8080/routing/CZE/ITA

# Route from Portugal to China
curl http://localhost:8080/routing/PRT/CHN

# Route with no land connection (will return 400)
curl http://localhost:8080/routing/USA/JPN
```

### Using a web browser

Simply navigate to:
- http://localhost:8080/routing/CZE/ITA
- http://localhost:8080/routing/FRA/CHN

## Algorithm

The service uses **Breadth-First Search (BFS)** algorithm to find the shortest path between countries:

1. Loads country data with border information at startup
2. Builds a graph where each country is a node and borders are edges
3. Uses BFS to find the shortest path (minimum border crossings)
4. Returns the complete route or HTTP 400 if no path exists

**Time Complexity**: O(V + E) where V is the number of countries and E is the number of borders  
**Space Complexity**: O(V) for the visited set and queue

## Project Structure

```
src/
├── main/
│   ├── java/com/test/routing/
│   │   ├── RoutingApplication.java          # Main Spring Boot application
│   │   ├── controller/
│   │   │   └── RoutingController.java       # REST API endpoint
│   │   ├── service/
│   │   │   ├── CountryService.java          # Loads and manages country data
│   │   │   └── RoutingService.java          # BFS routing algorithm
│   │   ├── model/
│   │   │   └── Country.java                 # Country data model
│   │   └── dto/
│   │       └── RouteResponse.java           # API response format
│   └── resources/
│       ├── application.yml                   # Application configuration
│       └── countries.json                    # Country border data
└── test/
    └── java/
```

## Configuration

The application can be configured in `src/main/resources/application.yml`:

- **Server port**: Default is 8080
- **Logging level**: Default is INFO for the application

## Testing

Run the tests with:

```bash
mvn test
```

## Notes

- Country codes are case-insensitive (both `CZE` and `cze` work)
- The service validates that both origin and destination countries exist
- If origin equals destination, it returns a route with a single country
- The BFS algorithm guarantees the shortest path (minimum border crossings)
