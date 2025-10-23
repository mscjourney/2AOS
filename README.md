# [2AOS](https://github.com/mscjourney/2AOS)

## Team members:

| Name | UNI |
|---|---|
| SeungHyun Sung | ss***4 |
| Anthony Marello | ap***9 |
| Anya Khatri | ak***9 |
| Osarobo Omokaro | oo***3 |

## Language and Platform

**Programming Language:**  Java

**Platform:**  Linux


# TARS Service

A Spring Boot REST API for storing user weather preference profiles and retrieving weather recommendations and alerts.

## Contents
- Features
- Requirements
- Build & Run
- Configuration
- Data Storage
- API Reference
  - Users
  - Weather Recommendation
  - Weather Alerts
  - Client Placeholder Endpoints
- Domain Models (JSON shapes)
- Examples (curl)
- Testing
- Common Issues
- Notes & Caveats
- Roadmap
- License

---

## Features
- Persist user weather, temperature, and city preferences (JSON file storage).
- Retrieve user preference profiles by ID.
- Generate weather recommendations (model-based stub).
- Retrieve weather alerts by city or coordinates.
- Configurable data file path via `tars.data.path`.

## Requirements
- Java 17+
- Maven 3.8+
- curl (optional for manual endpoint testing)

## Build & Run

```bash
# From repository root
cd TeamProject
mvn clean package

# Run (development)
mvn spring-boot:run

# Or run the built JAR
java -jar target/Tars-0.0.1-SNAPSHOT.jar
```

Default port: `8080`

---

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `tars.data.path` | `src/main/resources/data/userPreferences.json` (override recommended) | Path to JSON file for user persistence |

Override examples:

```properties
# application.properties
tars.data.path=./data/userPreferences.json
```

Environment variable + command line:
```bash
export TARS_DATA_PATH=./data/userPreferences.json
mvn spring-boot:run -Dtars.data.path=${TARS_DATA_PATH}
```

Recommended:
- Runtime: `./data/userPreferences.json`
- Tests: `target/test-userPreferences.json`

---

## Data Storage

- Users stored as a JSON array in the configured file.
- File and parent directory created if missing (initialized to `[]`).
- Full overwrite on each change (non-atomic).
- Not suitable for large scale or high concurrency without refactoring.

---

## API Reference

Base URL: `http://localhost:8080`

### Users

| Method | Endpoint | Description | Request Body | Success (200) | Errors |
|--------|----------|-------------|--------------|---------------|--------|
| GET | `/user/{id}` | Retrieve user preferences by ID | — | User JSON | 404 (not found) |
| PUT | `/user/{id}/add` | Create a user with given ID | User JSON | User JSON | 409 (duplicate), 400 (invalid body) |

### Weather Recommendation

| Method | Endpoint | Params | Description | Success (200) | Errors |
|--------|----------|--------|-------------|---------------|--------|
| GET | `/recommendation/weather` | `city` (string), `days` (1–14) | Get recommendation data | Recommendation JSON | 400 (bad params), 500 (internal) |

### Weather Alerts

| Method | Endpoint | Params | Description | Success (200) | Errors |
|--------|----------|--------|-------------|---------------|--------|
| GET | `/alert/weather` | Either `city` OR (`lat`, `lon`) | Retrieve current alerts | Alert JSON | 400 (invalid/missing params), 500 (internal) |

### Client Placeholder Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/clients/{clientId}` | Placeholder response |
| POST | `/clients/{clientId}/users/{userId}` | Placeholder response |
| GET | `/` or `/index` | Welcome message |

---

## Domain Models

### User
```json
{
  "id": 2,
  "weatherPreferences": ["rainy"],
  "temperaturePreferences": ["60F", "67F"],
  "cityPreferences": ["New York", "Paris"]
}
```

### WeatherAlert
```json
{
  "location": "New York",
  "alerts": [
    {
      "severity": "INFO",
      "type": "CLEAR",
      "message": "No weather alerts at this time"
    }
  ],
  "recommendations": [
    "Great weather for outdoor activities"
  ],
  "currentConditions": {
    "temperature_celsius": 22.5,
    "humidity_percent": 65.0,
    "wind_speed_kmh": 15.0,
    "precipitation_mm": 0.0,
    "weather_code": 1
  }
}
```

### WeatherRecommendation (example)
```json
{
  "city": "Boston",
  "daysRequested": 3,
  "recommendedDays": ["2025-10-22", "2025-10-23"],
  "notes": "Bring a light jacket"
}
```

---

## curl Examples

### Add a User
```bash
curl -X PUT http://localhost:8080/user/3/add \
  -H "Content-Type: application/json" \
  -d '{"id":3,"weatherPreferences":["snowy"],"temperaturePreferences":["88F","15C"],"cityPreferences":["Rome","Sydney"]}'
```

### Get a User
```bash
curl http://localhost:8080/user/3
```

### Weather Recommendation
```bash
curl "http://localhost:8080/recommendation/weather?city=Boston&days=3"
```

### Weather Alerts (City)
```bash
curl "http://localhost:8080/alert/weather?city=New%20York"
```

### Weather Alerts (Coordinates)
```bash
curl "http://localhost:8080/alert/weather?lat=40.7128&lon=-74.0060"
```

---

## Testing

```bash
cd TeamProject
mvn clean test
```

Test classes:
- `UserTest` – user add/retrieve flow.
- `AlertTest` – alert endpoint slice test (static mocks + `@WebMvcTest`).

Test data isolation from production data (recommended and implemented):
```properties
# src/test/resources/application.properties
tars.data.path=target/test-userPreferences.json
```

---