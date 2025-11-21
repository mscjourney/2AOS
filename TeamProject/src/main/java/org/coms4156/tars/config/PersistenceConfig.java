package org.coms4156.tars.config;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import org.coms4156.tars.service.TarsUserService;
import org.coms4156.tars.service.ClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

  @Bean
  public TarsUserService.FileMover tarsUserFileMover() {
    return (Path src, Path dest, CopyOption... opts) -> Files.move(src, dest, opts);
  }

  @Bean
  public ClientService.FileMover clientFileMover() {
    return (Path src, Path dest, CopyOption... opts) -> Files.move(src, dest, opts);
  }
}