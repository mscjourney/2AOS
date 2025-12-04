package org.coms4156.tars.controller;

/**
 * Helper to provide a test client API key used by integration tests.
 */
public final class TestKeys {
  private static String cachedClientKey;

  private TestKeys() {}

  /**
   * Retrieves the test client API key from data/clients.json.
   *
   * @return the first client's API key, or a fallback key if unavailable
   */
  public static String clientKey() {
    if (cachedClientKey != null) {
      return cachedClientKey;
    }
    try {
      // Read first client's key from data/clients.json
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      java.io.File f = new java.io.File("./data/clients.json");
      com.fasterxml.jackson.core.type.TypeReference<java.util.List<
          org.coms4156.tars.model.Client>> typeRef =
          new com.fasterxml.jackson.core.type.TypeReference<
              java.util.List<org.coms4156.tars.model.Client>>() {};
      java.util.List<org.coms4156.tars.model.Client> clients =
          mapper.readValue(f, typeRef);
      if (!clients.isEmpty()) {
        cachedClientKey = clients.get(0).getApiKey();
        return cachedClientKey;
      }
    } catch (Exception ignored) { /* no-op for test */ }
    // Fallback to a known test key if data file missing
    cachedClientKey = "clientkey000000000000000000000000";
    return cachedClientKey;
  }
}
