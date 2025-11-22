package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.service.ClientService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * {@code ClientServiceAtomicFallbackTest} Tests atomic move fallback
 * behavior when atomic operations are not supported for client data.
 */
public class ClientServiceAtomicFallbackTest {

  /**
   * {@code atomicFallbackLogsWarnTest} Verifies that when atomic move
   * fails, the service falls back to non-atomic move and logs a warning.
   */
  @Test
  public void atomicFallbackLogsWarnTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    Path tempDir = Files.createTempDirectory("client-fallback-test");
    Path dataFile = tempDir.resolve("clients.json");

    // Custom mover throwing AtomicMoveNotSupportedException on atomic attempts
    ClientService.FileMover failingAtomicMover = (src, dest, opts) -> {
      boolean atomic = false;
      for (CopyOption opt : opts) {
        if (opt == StandardCopyOption.ATOMIC_MOVE) {
          atomic = true;
          break;
        }
      }
      if (atomic) {
        throw new AtomicMoveNotSupportedException(src.toString(), dest.toString(), "simulated");
      }
      Files.move(src, dest, opts);
    };

    ClientService service = new ClientService(dataFile.toString(), failingAtomicMover);
    // Trigger saveData via creation
    Client c = service.createClient("FallbackClient", "fallback@example.com");
    assertTrue(c != null);

    assertTrue(
        appender.list.stream().anyMatch(ev ->
            ev.getLevel() == Level.WARN
                && ev.getFormattedMessage().contains("Atomic move not supported for clients file")),
        "Should log WARN for atomic fallback on clients file");

    logger.detachAppender(appender);
    Files.deleteIfExists(dataFile);
    Files.deleteIfExists(tempDir);
  }
}