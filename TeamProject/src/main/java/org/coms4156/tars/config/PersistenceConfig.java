package org.coms4156.tars.config;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code PersistenceConfig} Configuration for persistence-related beans.
 * Provides FileMover implementations for service layer file operations.
 */
@Configuration
public class PersistenceConfig {

  /**
   * {@code tarsUserFileMover} Provides a FileMover bean for TarsUserService.
   *
   * @return FileMover implementation using Files.move
   */
  @Bean
  public TarsUserService.FileMover tarsUserFileMover() {
    return Files::move;
  }

  /**
   * {@code clientFileMover} Provides a FileMover bean for ClientService.
   *
   * @return FileMover implementation using Files.move
   */
  @Bean
  public ClientService.FileMover clientFileMover() {
    return Files::move;
  }
}