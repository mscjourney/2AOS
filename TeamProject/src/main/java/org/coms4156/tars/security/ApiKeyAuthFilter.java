package org.coms4156.tars.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.coms4156.tars.model.Client;
import org.springframework.lang.NonNull;
import org.coms4156.tars.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code ApiKeyAuthFilter} Enforces API key authentication on protected routes.
 * Bypasses configured public paths. Attaches resolved Client to request attributes.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

  private final ClientService clientService;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String headerName;
    @Value("${security.enabled:true}")
    private boolean securityEnabled;
  private final Set<String> publicPaths;
  private final Set<String> adminKeys;
  private final java.util.concurrent.ConcurrentMap<Long, RateBucket> rateBuckets = new java.util.concurrent.ConcurrentHashMap<>();

  public ApiKeyAuthFilter(
      ClientService clientService,
      @Value("${security.apiKey.header:X-API-Key}") String headerName,
      @Value("${security.publicPaths:/,/index,/health}") String publicPathsCsv,
      @Value("${security.adminApiKeys:}") String adminKeysCsv
  ) {
    this.clientService = clientService;
    this.headerName = headerName;
    this.publicPaths = new HashSet<>(Arrays.asList(publicPathsCsv.split(",")));
    this.adminKeys = adminKeysCsv == null || adminKeysCsv.isBlank()
        ? java.util.Collections.emptySet()
        : new HashSet<>(Arrays.asList(adminKeysCsv.split(",")));
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    if (publicPaths.contains(path)) {
      return true;
    }
    // Allow static resources
    return path.startsWith("/static/") || path.startsWith("/assets/") || path.startsWith("/favicon");
  }

  @Override
    protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Allow disabling security for testing via property
        if (!securityEnabled) {
          filterChain.doFilter(request, response);
          return;
        }
    String apiKey = request.getHeader(headerName);
    if (apiKey == null || apiKey.isBlank()) {
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "API key required");
      return;
    }

    Client client = clientService.findByApiKey(apiKey);
    boolean adminKey = adminKeys.contains(apiKey);
    if (client == null && !adminKey) {
      writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
      return;
    }

    // Admin-only guard for /clients/** endpoints
    String path = request.getRequestURI();
    if ("/clients".equals(path) || path.startsWith("/clients/")
        || "".equals(path) // placeholder to avoid style error on next line
        || path.startsWith("/client/")) {
      if (!adminKey) {
        writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
        return;
      }
    }

    // Simple per-minute rate limiting per client
    if (!path.startsWith("/static/") && !publicPaths.contains(path)) {
      long bucketKey;
      if (client != null && client.getClientId() != null) {
        bucketKey = client.getClientId();
      } else {
        bucketKey = -1L; // admin/synthetic or missing id
      }
      RateBucket bucket = rateBuckets.computeIfAbsent(bucketKey, k -> new RateBucket());
      int limit = (client != null) ? client.getRateLimitPerMinute() : Integer.MAX_VALUE;
      if (!bucket.allow(limit)) {
        writeError(response, request, 429, "Rate limit exceeded");
        return;
      }
    }

    // Attach client to request for downstream use
    if (client == null && adminKey) {
      // Synthetic admin client context
      Client admin = new Client(-1L, "admin", "admin@tars.local", apiKey);
      request.setAttribute("authenticatedClient", admin);
    } else {
      request.setAttribute("authenticatedClient", client);
    }

    filterChain.doFilter(request, response);
  }

  private void writeError(HttpServletResponse response, HttpServletRequest request, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    var body = new java.util.HashMap<String, Object>();
    body.put("status", status);
    body.put("error", status == 401 ? "Unauthorized" : status == 403 ? "Forbidden" : "Error");
    body.put("message", message);
    body.put("path", request.getRequestURI());
    body.put("timestamp", java.time.Instant.now().toString());
    response.getWriter().write(mapper.writeValueAsString(body));
    if (logger.isWarnEnabled()) {
      logger.warn("API key auth failed: status={} path={} msg={}", status, request.getRequestURI(), message);
    }
  }

  private static final class RateBucket {
    private long windowStartMillis = System.currentTimeMillis();
    private int count = 0;

    synchronized boolean allow(int limitPerMinute) {
      long now = System.currentTimeMillis();
      long windowMillis = 60_000L;
      if (now - windowStartMillis >= windowMillis) {
        windowStartMillis = now;
        count = 0;
      }
      if (count >= Math.max(1, limitPerMinute)) {
        return false;
      }
      count++;
      return true;
    }
  }
}