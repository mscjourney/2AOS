package org.coms4156.tars.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.coms4156.tars.service.TarsUserService;
import org.coms4156.tars.model.TarsUser;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TarsUserServiceAtomicFallbackTest {

  @Test
  public void atomicFallbackLogsWarnTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    Path tempDir = Files.createTempDirectory("user-fallback-test");
    Path dataFile = tempDir.resolve("users.json");

    TarsUserService.FileMover failingAtomicMover = (src, dest, opts) -> {
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

    TarsUserService service = new TarsUserService(dataFile.toString(), failingAtomicMover);
    TarsUser user = service.createUser(1L, "fallback_user", "fallback@test.com", "user");
    assertTrue(user != null);

    assertTrue(appender.list.stream().anyMatch(ev ->
            ev.getLevel() == Level.WARN &&
                ev.getFormattedMessage().contains("Atomic move not supported for users file")),
        "Should log WARN for atomic fallback on users file");

    logger.detachAppender(appender);
    Files.deleteIfExists(dataFile);
    Files.deleteIfExists(tempDir);
  }
}