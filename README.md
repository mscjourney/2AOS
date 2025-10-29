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


# Travel Alert and Recommendation Service (TARS)

TARS is a Spring Boot REST API that lets clients:
- Register users with preference profiles (weather types, temperature ranges, favorite cities).
- Retrieve stored user profiles.
- Get weather recommendations (best upcoming days).
- Get real-time weather alerts for a location or for a user's saved cities.
- Fetch basic crime summary info for a state/offense.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Internet access (for external weather/crime data if implemented in models)

### Build & Run

```bash
mvn clean package
mvn spring-boot:run
```

Server defaults to: `http://localhost:8080`

## Configuration

The user preferences are persisted as JSON.

Property | Purpose | Default
-------- | ------- | -------
`tars.data.path` | Filesystem path for user preferences JSON | `./data/userPreferences.json`

Override in `src/main/resources/application.properties`:
```properties
tars.data.path=./data/userPreferences.json
```

For test isolation (already present):
```properties
# src/test/resources/application.properties
tars.data.path=target/test-userPreferences.json
```

Directory `./data` is created automatically if missing.

## Data Model Overview

### User
```json
{
  "id": 1,
  "weatherPreferences": ["Sunny", "Rain"],
  "temperaturePreferences": ["Mild", "Cold"],
  "cityPreferences": ["New York", "Boston"]
}
```

- `id` must be non-negative and unique.
- Lists may be empty; null lists are normalized to empty.

### WeatherRecommendation
```json
{
  "city": "Raleigh",
  "recommendedDays": ["2025-10-24", "2025-10-26"],
  "message": "2 clear days found in the next 5-day window."
}
```

### WeatherAlert
```json
{
  "location": "Raleigh",
  "timestamp": "Thu Oct 23 10:15:00 EDT 2025",
  "alerts": [
    { "severity": "Moderate", "type": "Wind", "message": "Gusts up to 30mph" }
  ],
  "recommendations": ["Delay outdoor activities", "Secure loose items"],
  "currentConditions": {
    "temperatureF": 57.2,
    "humidity": 0.62,
    "status": "Windy"
  }
}
```

### CrimeSummary
```json
{
  "state": "NC",
  "month": "10",
  "year": "2025",
  "message": "Fetched crime data successfully for ASS : <raw API summary>"
}
```

## Endpoints

Base path: `http://localhost:8080`

### Health / Index
GET `/` or `/index`  
Response: `"Welcome to the TARS Home Page!"`

### Users

PUT `/user/{id}/add`  
Add a new user profile.

Request body:
```json
{
  "id": 5,
  "weatherPreferences": ["Rain", "Cloudy"],
  "temperaturePreferences": ["Cool"],
  "cityPreferences": ["Chicago"]
}
```

Responses:
- `200 OK` – returns created user JSON
- `409 CONFLICT` – ID already exists
- `400 BAD REQUEST` – malformed body (implicit via validation exceptions if added later)

GET `/user/{id}`  
Retrieve a user by ID.

Responses:
- `200 OK` – user JSON
- `404 NOT FOUND` – user absent

GET `/userList`
Retrieve all existing users

Reponses:
- `200 OK` - list of users JSON
- `500 INTERNAL SERVER ERROR` - unexpected failure

### Weather Recommendation

GET `/recommendation/weather?city={city}&days={days}`

Query params:
- `city` (required)
- `days` (required, 1–14)

Responses:
- `200 OK` – `WeatherRecommendation`
- `400 BAD REQUEST` – invalid `days` range (≤0 or >14)
- `500 INTERNAL SERVER ERROR` – unexpected failure

Example:
```
GET /recommendation/weather?city=Raleigh&days=5
```

### Weather Alerts (Location)

GET `/alert/weather?city={city}`  
OR  
GET `/alert/weather?lat={lat}&lon={lon}`

Rules:
- Either `city` OR both `lat` and `lon` must be provided.

Responses:
- `200 OK` – `WeatherAlert`
- `400 BAD REQUEST` – missing or invalid parameters
- `500 INTERNAL SERVER ERROR` – unexpected failure

### Weather Alerts (User Preferences)

GET `/alert/weather/user/{userId}`

Uses the user's `cityPreferences` to aggregate alerts.

Responses:
- `200 OK` – list of `WeatherAlert` objects
- `400 BAD REQUEST` – negative user ID
- `404 NOT FOUND` – no such user
- `500 INTERNAL SERVER ERROR` – unexpected failure

### Crime Summary

GET `/crime/summary?state={state}&offense={offense}&month={MM}&year={YYYY}`

Parameters:
- `state`: US state abbreviation (e.g., `NC`, `CA`)
- `offense`: allowed codes (e.g., `ASS`, `BUR`, `HOM`, `ROB`, `RPE`, `LAR`, `MVT`, `ARS`, `V`, `P`)
- `month`: two-digit month `01`–`12`
- `year`: four-digit year

Responses:
- `200 OK` – `CrimeSummary`
- `500 INTERNAL SERVER ERROR` – failure

Example:
```
GET /crime/summary?state=NC&offense=ASS&month=10&year=2025
```

## Persistence Behavior

- Users stored in JSON file at `tars.data.path`.
- On startup: if file missing, a new empty JSON array is created.
- All write operations (`addUser`) are synchronized for thread safety.
- Reads return defensive copies (mutating returned list does not persist changes).

## Error Handling Summary

Error Type | Cause | HTTP Code
---------- | ----- | ---------
Duplicate user ID | Existing ID match | 409
User not found | ID absent in store | 404
Invalid days | `days <= 0 || days > 14` | 400
Missing alert params | Neither city nor lat/lon | 400
Negative user ID | `userId < 0` | 400
Unexpected exception | Uncaught runtime errors | 500

## Example cURL Session

Create user:
```bash
curl -X PUT http://localhost:8080/user/7/add \
  -H "Content-Type: application/json" \
  -d '{"id":7,"weatherPreferences":["Sunny"],"temperaturePreferences":["Warm"],"cityPreferences":["Austin"]}'
```

Fetch user:
```bash
curl http://localhost:8080/user/7
```

Weather recommendation:
```bash
curl "http://localhost:8080/recommendation/weather?city=Austin&days=5"
```

Weather alerts by city:
```bash
curl "http://localhost:8080/alert/weather?city=Austin"
```

Weather alerts by user:
```bash
curl http://localhost:8080/alert/weather/user/7
```

Crime summary:
```bash
curl "http://localhost:8080/crime/summary?state=TX&offense=ASS&month=10&year=2025"
```

## Testing

Run all tests:
```bash
mvn test
```

Test config overrides persistence path (`target/test-userPreferences.json`) to avoid polluting real data.

## Extensibility Ideas

- Implement client-to-user relationship in `/clients` routes.
- Add authentication and rate limiting.

## License

See `LICENSE` file in repository.

## Other Resources

- [JIRA Board](https://2aos.atlassian.net/jira/software/projects/SCRUM/boards/1)
- [PMD output result](TeamProject/pmd.txt)

For new PMD result, from within the `TeamProject` directory, run:

```
pmd check -d './'  -R rulesets/java/quickstart.xml -r pmd.txt
```

---