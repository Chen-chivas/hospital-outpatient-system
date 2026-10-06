package com.example.ocs.common.web;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final String[] allowedOriginPatterns;

  public WebConfig(@Value("${ocs.cors.allowed-origins:*}") String allowedOrigins) {
    this.allowedOriginPatterns = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toArray(String[]::new);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns(allowedOriginPatterns)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .maxAge(3600);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/{path:^(?!api|outpatient|smart|h2-console|assets|css|js|images|webjars|index\\.html|favicon\\.ico|favicon\\.svg|icons\\.svg|error$).*$}")
        .setViewName("forward:/index.html");
    registry.addViewController("/{path:^(?!api|outpatient|smart|h2-console|assets|css|js|images|webjars|index\\.html|favicon\\.ico|favicon\\.svg|icons\\.svg|error$).*$}/**")
        .setViewName("forward:/index.html");
  }
}
