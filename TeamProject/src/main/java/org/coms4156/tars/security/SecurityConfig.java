package org.coms4156.tars.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers ApiKeyAuthFilter in the servlet filter chain for all routes.
 */
@Configuration
public class SecurityConfig {

  /**
   * Registers the API key authentication filter.
   *
   * @param apiKeyAuthFilter the API key auth filter to register
   * @return the filter registration bean
   */
  @Bean
  public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(
      ApiKeyAuthFilter apiKeyAuthFilter) {
    FilterRegistrationBean<ApiKeyAuthFilter> registration =
        new FilterRegistrationBean<>(apiKeyAuthFilter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.addUrlPatterns("/*");
    registration.setName("apiKeyAuthFilter");
    return registration;
  }
}
