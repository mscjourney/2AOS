# TARS Backend (TeamProject)

This README documents the Spring Boot backend: security model, configuration, endpoints, and concrete usage examples.

For a high-level project overview and quick start, see the root `README.md`.

---

## Use Case

TARS serves multi-tenant clients who authenticate with API keys. Each client can create and manage tenant-scoped users and store their travel/weather preferences. The service provides:
- Weather recommendations by city and days range
- Real-time weather alerts by city or coordinates
- Aggregated alerts for a user’s saved cities
- Crime summaries by state/offense
- Client and user management (admin-gated)

---

## Security: API Key Authentication

All protected routes require an API key header. Two types of keys:
- Client API keys: per-client, authorize standard endpoints
- Admin API keys: configured in properties, grant `/clients/**` and admin actions

Header name is configurable; default is `X-API-Key`.

Public paths (no key required): `/`, `/index`, `/health`, `/login`.

---

## Configuration

Edit `src/main/resources/application.properties` to configure security and data paths:

```properties
# API key configuration
security.apiKey.header=X-API-Key
security.publicPaths=/,/index,/health,/login
security.adminApiKeys=adminkey000000000000000000000000

# Data storage (defaults shown)
tars.data.path=./data/userPreferences.json
tars.users.path=./data/users.json
```

Test isolation uses `target/test-userPreferences.json` for preferences.

---

## Admin Endpoints (require Admin API key)

- `GET /clients` – list all clients
- `GET /clients/{clientId}` – fetch a single client
- `POST /clients/{clientId}/rotateKey` – rotate client API key
- `POST /clients/{clientId}/setRateLimit` – set rate limit `{ "limit": <1-10000> }`
- `POST /client/create` – create a new client
- `POST /client/createUser` – create a tenant user for a client

Example: Rotate client key
```bash
curl -X POST \
  -H "X-API-Key: <admin-api-key>" \
  http://localhost:8080/clients/123/rotateKey
```

Example: Set rate limit
```bash
curl -X POST \
  -H "X-API-Key: <admin-api-key>" \
  -H "Content-Type: application/json" \
  -d '{"limit":25}' \
  http://localhost:8080/clients/123/setRateLimit
```

---

## Client/User Endpoints (require Client API key)

### User Preferences

PUT `/setPreference/{id}`
```bash
curl -X PUT \
  -H "X-API-Key: <client-api-key>" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"weatherPreferences":["sunny"],"temperaturePreferences":["70F"],"cityPreferences":["Boston"]}' \
  http://localhost:8080/setPreference/1
```

GET `/clearPreference/{id}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  http://localhost:8080/clearPreference/1
```

GET `/retrievePreference/{id}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  http://localhost:8080/retrievePreference/1
```

GET `/userPreferenceList`
```bash
curl -H "X-API-Key: <client-api-key>" \
  http://localhost:8080/userPreferenceList
```

### Weather Recommendation

GET `/recommendation/weather?city={city}&days={days}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/recommendation/weather?city=Boston&days=5"
```

Days must be in range `1..14`.

### Weather Alerts

GET `/alert/weather?city={city}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/alert/weather?city=Raleigh"
```

GET `/alert/weather?lat={lat}&lon={lon}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/alert/weather?lat=40.7128&lon=-74.0060"
```

### Weather Alerts by User

GET `/alert/weather/user/{userId}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/alert/weather/user/2"
```

### Crime Summary

GET `/crime/summary?state={state}&offense={offense}&month={MM}&year={YYYY}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/crime/summary?state=NC&offense=ASS&month=10&year=2025"
```

### Country Alerts

GET `/country/{country}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/country/France"
```

GET `/countrySummary/{country}`
```bash
curl -H "X-API-Key: <client-api-key>" \
  "http://localhost:8080/countrySummary/France"
```

---

## Error Handling (selected)

Error Type | HTTP Code
---------- | ---------
Missing/invalid API key on protected route | 401
Admin route with client key | 403
Duplicate client name | 409
Duplicate username (per client) | 409
Client not found | 404
User not found | 404
Invalid days range | 400
Missing alert params | 400
Unexpected exception | 500

---

## Testing

Security-focused tests:
- `ApiKeyAuthFilterTest` – protected vs public routes, header required
- `ApiKeyHeaderConfigTest` – custom header honored
- `ClientKeyRotationTest` – rotate returns new key, 401 without header
- `AdminAndRateLimitTest` – admin-only `/clients/**`, per-client rate limiting (429)
- `ClientsAdminAccessTest` – admin gating for `/clients`, `/clients/{id}`, `/client/create`

Run:
```bash
cd TeamProject
mvn test
```

---

## Notes & Data

- Client data: `TeamProject/data/clients.json`
- Users: `TeamProject/data/users.json`
- Preferences: `TeamProject/data/userPreferences.json`

---

## README Separation

Yes, the separation is intentional and helpful:
- Root `README.md`: project overview, setup, and quick usage across modules.
- `TeamProject/README.md`: backend service details (security, configuration, endpoints, admin usage).

Keeping them separate avoids mixing high-level guidance with deep backend specifics and makes navigation clearer for contributors and consumers.
