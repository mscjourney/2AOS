# TARS Security: API Key Authentication

TARS enforces API key authentication for all non-public routes via an application filter. Clients must include their API key in the configured header to access protected endpoints.

- Header name: configured via `security.apiKey.header` (default `X-API-Key`).
- Public paths: configured via `security.publicPaths` (default `/,/index,/health,/login`). All other paths are protected.

## Managing Client API Keys

- List clients: `GET /clients`
- Get client: `GET /clients/{clientId}`
- Create client: `POST /client/create` with JSON `{"name":"...","email":"..."}`
- Rotate API key: `POST /clients/{clientId}/rotateKey`

Admin-only access: Endpoints under `/clients/**` require an admin API key configured via `security.adminApiKeys`. Admin keys do not need entries in `clients.json`.

The rotate endpoint requires a valid API key header and returns JSON with the new key:

```json
{
  "clientId": "123",
  "apiKey": "<new-32-hex-key>"
}
```

## Configuration

Edit `src/main/resources/application.properties`:

```
security.apiKey.header=X-API-Key
security.publicPaths=/,/index,/health,/login
security.adminApiKeys=adminkey000000000000000000000000
```

## Testing

Targeted security tests verify enforcement and configuration:
- `ApiKeyAuthFilterTest` – protected routes require keys; public and static paths bypass.
- `ApiKeyHeaderConfigTest` – custom header name is honored.
- `ClientKeyRotationTest` – rotate endpoint returns a new key; 401 without header.
 - `AdminAndRateLimitTest` – admin-only access to `/clients/**` and per-client rate limiting returning `429` when exceeded.
 - `ClientsAdminAccessTest` – admin-only gating for `/clients`, `/clients/{id}`, and `/client/create`.

## Admin-Only Endpoints

The following endpoints are accessible only with an admin API key configured via `security.adminApiKeys`:

- `GET /clients`
- `GET /clients/{clientId}`
- `POST /clients/{clientId}/rotateKey`
 - `POST /clients/{clientId}/setRateLimit` with body `{ "limit": <1-10000> }`
- `POST /client/create`
 - `POST /clients/{clientId}/setRateLimit`

Admin keys do not require entries in `clients.json` and are validated purely by configuration.

### Example: Set Rate Limit

Request:

```
POST /clients/123/setRateLimit
Header: X-API-Key: <admin-key>
Body: { "limit": 25 }
```

Response:

```
200 OK
{ "clientId": 123, "name": "...", "email": "...", "rateLimitPerMinute": 25, "maxConcurrentRequests": 5 }
```
 - `AdminAndRateLimitTest` – admin-only access to `/clients/**` and per-client rate limiting returns `429` when exceeded.

Run the tests:

```bash
cd TeamProject
mvn -Dtest=org.coms4156.tars.security.ApiKeyAuthFilterTest,org.coms4156.tars.security.ApiKeyHeaderConfigTest,org.coms4156.tars.security.ClientKeyRotationTest test
```

## Notes

- Client data persists in `./data/clients.json`. Seed clients there during development.
- Consider rate limiting per client using existing `rateLimitPerMinute` and `maxConcurrentRequests` fields.
