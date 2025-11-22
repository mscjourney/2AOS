package org.coms4156.tars.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

/**
 * {@code LoggerTestUtil} Helper for capturing and asserting controller logs.
 * Use try-with-resources: capture(RouteController.class, Level.DEBUG).
 */
public final class LoggerTestUtil {

  private LoggerTestUtil() {}

  /**
   * {@code capture} Attaches a ListAppender and sets level; restores on close.
   *
   * @param loggerClass class whose logger to capture
   * @param level log level to force for the duration
   * @return captured logger wrapper
   */
  public static CapturedLogger capture(Class<?> loggerClass, Level level) {
    Logger logger = (Logger) LoggerFactory.getLogger(loggerClass);
    final Level original = logger.getLevel();
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(level);
    return new CapturedLogger(logger, original, appender);
  }

  /**
   * {@code CapturedLogger} Auto-closeable wrapper for captured logging events.
   */
  public static final class CapturedLogger implements AutoCloseable {
    private final Logger logger;
    private final Level originalLevel;
    private final ListAppender<ILoggingEvent> appender;

    private CapturedLogger(
        Logger logger,
        Level originalLevel,
        ListAppender<ILoggingEvent> appender) {
      this.logger = logger;
      this.originalLevel = originalLevel;
      this.appender = appender;
    }

    /**
     * {@code events} Returns immutable snapshot of logging events list.
     *
     * @return list of events
     */
    public List<ILoggingEvent> events() {
      return appender.list;
    }

    /**
     * {@code stream} Returns a stream of logging events for custom assertions.
     *
     * @return stream of events
     */
    public Stream<ILoggingEvent> stream() {
      return appender.list.stream();
    }

    /**
     * {@code contains} Checks if any event of given level contains fragment.
     *
     * @param level target level
     * @param fragment substring to search
     * @return true if match found
     */
    public boolean contains(Level level, String fragment) {
      return stream().anyMatch(e ->
          e.getLevel() == level && e.getFormattedMessage().contains(fragment));
    }

    /**
     * {@code hasLevel} Checks if any event logged at specified level.
     *
     * @param level level to check
     * @return true if present
     */
    public boolean hasLevel(Level level) {
      return stream().anyMatch(e -> e.getLevel() == level);
    }

    /**
     * {@code close} Restores original level and detaches appender.
     */
    @Override
    public void close() {
      logger.detachAppender(appender);
      appender.stop();
      logger.setLevel(originalLevel);
    }
  }
}